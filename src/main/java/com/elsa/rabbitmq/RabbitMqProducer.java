package com.elsa.rabbitmq;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsa.rabbitmq.domain.MqConstants;
import com.elsa.rabbitmq.domain.MqMessageConfigs;
import com.elsa.rabbitmq.domain.MqP2pConfig;
import com.elsa.rabbitmq.domain.MqTopicConfig;
import com.elsa.rabbitmq.exception.MqClientException;
import com.elsa.rabbitmq.util.MqNameUtil;
import com.elsa.rabbitmq.util.MqProductConnectionSingleton;
import com.elsa.rabbitmq.util.MqQueueHaPolicySingleton;
import com.elsa.rabbitmq.util.MqSerializeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;

/**
 * @author longhaisheng
 *
 */
public class RabbitMqProducer {

	private static final Log logger = LogFactory.getLog(RabbitMqProducer.class);

	private MqMessageConfigs messageConfigs;

	private SendMessageFailCallBack sendMessageFailCallBack;

	private Gson gson = new GsonBuilder().registerTypeAdapter(Timestamp.class, new TimestampTypeAdapter())
			.setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	public MqMessageConfigs getMessageConfigs() {
		return messageConfigs;
	}

	public void setMessageConfigs(MqMessageConfigs messageConfigs) {
		this.messageConfigs = messageConfigs;
	}

	private boolean mandatory = false;

	public void sendP2PMessage(String oldQueueName, final Object obj) throws MqClientException {
		this.sendP2PMessage(oldQueueName, obj, null);
	}

	/**
	 * 异步发送点对点消息
	 * 
	 * @param oldQueueName
	 *            队列名
	 * @param obj
	 *            要发送的消息对象
	 * @param nackMessage 记录nack消息的日志内容
	 * @throws IOException
	 */
	public void sendP2PMessage(String oldQueueName, final Object obj, final String nackMessage) throws MqClientException {
		if (null == obj) {
			String message = oldQueueName + " send message must not null ";
			logger.error(message);
			throw new MqClientException(message);
		}
		if (!(obj instanceof Serializable)) {
			String message = oldQueueName + " send message must instanceof Serializable interface ";
			logger.error(message);
			throw new MqClientException(message);
		}
		if (this.getMessageConfigs().existsP2p()) {
			String message = oldQueueName + "sendP2PMessage 队列名配置重复";
			logger.error(message);
			throw new MqClientException(message);
		}
		if (null != oldQueueName && !"".equals(oldQueueName)) {
			Channel channel = null;
			boolean messageBurable = true;
			BasicProperties baseProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
			MqP2pConfig queueConfigDO = messageConfigs.getQueueConfigDO(oldQueueName);
			if (!queueConfigDO.getSendMessageType().equalsIgnoreCase(MqConstants.SEND_MESSAGE_TYPE_P2P)) {
				String message = "oldQueueName=>" + oldQueueName + "send message type must " + MqConstants.SEND_MESSAGE_TYPE_P2P;
				logger.error(message);
				throw new MqClientException(message);
			}
			String exchangeName = MqNameUtil.getExchangeName(oldQueueName);
			String newQueueName = MqNameUtil.getQueueName(oldQueueName);
			String routingKey = MqNameUtil.getRoutingKey(oldQueueName);
			try {
				channel = MqProductConnectionSingleton.connection().createChannel();

				if (!queueConfigDO.isQueueDurable()) {
					messageBurable = false;
					baseProp = null;
				}
				if (queueConfigDO.isQueueDurable() && queueConfigDO.isPublishConfirm()) {
					channel.addConfirmListener(new ConfirmListener() {
						public void handleNack(long deliveryTag, boolean multiple) throws IOException {
							if (StringUtils.isNoneBlank(nackMessage)) {
								logger.info("Nack msg=>" + nackMessage);
							} else {
								logger.info("Nack msg=>" + gson.toJson(obj));
							}
						}

						public void handleAck(long deliveryTag, boolean multiple) throws IOException {
							logger.info("Ack, SeqNo: " + deliveryTag + ", multiple: " + multiple + " msg=>" + gson.toJson(obj));
						}
					});
					channel.confirmSelect();
				}
				channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);
				channel.queueDeclare(newQueueName, messageBurable, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());
				channel.queueBind(newQueueName, exchangeName, routingKey);
				byte[] bytes = MqSerializeUtil.serialize(obj);

				if (queueConfigDO.isMandatory()) {
					channel.basicPublish(exchangeName, routingKey, true, baseProp, bytes);
				} else {
					channel.basicPublish(exchangeName, routingKey, baseProp, bytes);
				}
				if (queueConfigDO.isQueueDurable() && queueConfigDO.isPublishConfirm()) {
					channel.waitForConfirmsOrDie();
				}
			} catch (IOException ex) {
				String message = "oldQueueName=>" + oldQueueName + "send message IOException for:" + ex.getMessage();
				logger.error(message, ex);
				throw new MqClientException(message, ex);
			} catch (Exception ex) {
				String message = "oldQueueName=>" + oldQueueName + "send message Exception for:" + ex.getMessage();
				logger.error(message, ex);
				throw new MqClientException(message, ex);
			} finally {
				try {
					if (null != channel && channel.isOpen()) {
						channel.close();
					}
				} catch (IOException ex) {
					String message = "queueName=" + oldQueueName + "channel close exception for " + ex.getMessage();
					logger.error(message);
					throw new MqClientException(message, ex);
				}
			}
		}
	}

	/**
	 * 同步发送点对点消息,确保消息无丢失，但性能较低
	 * 
	 * @param oldQueueName
	 *            队列名
	 * @param obj
	 *            要发送的消息对象
	 * @return true 消息发送成功，false消息发送失败
	 */
	public boolean sendSyncP2PMessage(String oldQueueName, final Object obj) {
		if (null == obj) {
			String message = oldQueueName + " send message must not null ";
			logger.error(message);
			return false;
		}
		if (!(obj instanceof Serializable)) {
			String message = oldQueueName + " send message must instanceof Serializable interface ";
			logger.error(message);
			return false;
		}
		if (this.getMessageConfigs().existsP2p()) {
			String message = oldQueueName + "sendP2PMessage 队列名配置重复";
			logger.error(message);
			return false;
		}
		boolean isSuccess = false;
		if (null != oldQueueName && !"".equals(oldQueueName)) {
			Channel channel = null;
			boolean messageBurable = true;
			BasicProperties baseProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
			MqP2pConfig queueConfigDO = messageConfigs.getQueueConfigDO(oldQueueName);
			if (!queueConfigDO.getSendMessageType().equalsIgnoreCase(MqConstants.SEND_MESSAGE_TYPE_P2P)) {
				String message = "oldQueueName=>" + oldQueueName + "send message type must " + MqConstants.SEND_MESSAGE_TYPE_P2P;
				logger.error(message);
				return false;
			}
			String exchangeName = MqNameUtil.getExchangeName(oldQueueName);
			String newQueueName = MqNameUtil.getQueueName(oldQueueName);
			String routingKey = MqNameUtil.getRoutingKey(oldQueueName);
			try {
				channel = MqProductConnectionSingleton.connection().createChannel();
				if (null != channel) {
					channel.txSelect();
					if (!queueConfigDO.isQueueDurable()) {
						messageBurable = false;
						baseProp = null;
					}
					channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);
					channel.queueDeclare(newQueueName, messageBurable, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());
					channel.queueBind(newQueueName, exchangeName, routingKey);
					byte[] bytes = MqSerializeUtil.serialize(obj);

					if (isMandatory()) {
						channel.basicPublish(exchangeName, routingKey, true, baseProp, bytes);
					} else {
						channel.basicPublish(exchangeName, routingKey, baseProp, bytes);
					}
					channel.txCommit();
					isSuccess = true;
				}
			} catch (Exception ex) {
				String message = "oldQueueName=>" + oldQueueName + "send message exception for:" + ex.getMessage();
				logger.error(message, ex);
				if (null != channel) {
					try {
						channel.txRollback();
					} catch (IOException e) {
						logger.error(message, e);
					}
				}
				isSuccess = false;
			} finally {
				try {
					if (channel != null) {
						channel.close();
					}
				} catch (Exception ex) {
					String message = "queueName=" + oldQueueName + "channel close exception for " + ex.getMessage();
					logger.error(message);
				}
			}
			return isSuccess;
		}
		return isSuccess;
	}

	/**
	 * 发送主题订阅消息 不支持持久化
	 * 
	 * @param queueName
	 *            队列名
	 * @param t
	 *            要发送的消息
	 * @throws IOException
	 */
	public void sendTopicMessage(String queueName, Object t) throws MqClientException {
		if (null == t) {
			String message = queueName + " send message must not null ";
			logger.error(message);
			throw new MqClientException(message);
		}
		if (null != queueName && !"".equals(queueName)) {
			if (!(t instanceof Serializable)) {
				String message = queueName + " send message must instanceof Serializable interface ";
				logger.error(message);
				throw new MqClientException(message);
			}
			if (this.getMessageConfigs().existsTopic()) {
				logger.error("sendTopicMessage 队列名配置重复");
				throw new MqClientException("sendTopicMessage 队列名配置重复");
			}

			MqTopicConfig topicConfigDO = messageConfigs.getTopicConfigDO(queueName);
			if (null != topicConfigDO && !topicConfigDO.getSendMessageType().equalsIgnoreCase(MqConstants.SEND_MESSAGE_TYPE_PUB_SUB)) {
				String message = "oldQueueName=>" + queueName + "send message type must " + MqConstants.SEND_MESSAGE_TYPE_PUB_SUB;
				logger.error(message);
				throw new MqClientException(message);
			}

			String exchangeName = MqNameUtil.getExchangeName(queueName);
			Channel channel = null;
			try {
				channel = MqProductConnectionSingleton.connection().createChannel();
				channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_FANOUT, true);

				byte[] bytes = MqSerializeUtil.serialize(t);
				channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
			} catch (Exception ex) {
				String message = "sendTopicMessage method queueName = " + queueName + " ;exchangeName=" + exchangeName + ",exception for "
						+ ex.getMessage();
				logger.error(message, ex);
				throw new MqClientException(message, ex);
			} finally {
				try {
					if (channel != null) {
						channel.close();
					}
				} catch (Exception ex) {
					String message = "sendTopicMessage method  queueName = " + queueName + ",channel close exception for "
							+ ex.getMessage();
					logger.error(message, ex);
					throw new MqClientException(message, ex);
				}
			}
		}

	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public SendMessageFailCallBack getSendMessageFailCallBack() {
		return sendMessageFailCallBack;
	}

	public void setSendMessageFailCallBack(SendMessageFailCallBack sendMessageFailCallBack) {
		this.sendMessageFailCallBack = sendMessageFailCallBack;
	}

}

class TimestampTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
	public JsonElement serialize(Timestamp src, Type arg1, JsonSerializationContext arg2) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateFormatAsString = format.format(new Date(src.getTime()));
		return new JsonPrimitive(dateFormatAsString);
	}

	public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonPrimitive)) {
			throw new JsonParseException("The date should be a string value");
		}

		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = (Date) format.parse(json.getAsString());
			return new Timestamp(date.getTime());
		} catch (Exception e) {
			throw new JsonParseException(e);
		}
	}

}
