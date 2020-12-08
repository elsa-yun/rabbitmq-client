package com.elsa.rabbitmq;
//package com.meitun.mq;
//
//import java.io.IOException;
//import java.io.Serializable;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.meitun.mq.domain.MqConstants;
//import com.meitun.mq.domain.MqMessageConfigs;
//import com.meitun.mq.domain.MqP2pConfig;
//import com.meitun.mq.domain.MqTopicConfig;
//import com.meitun.mq.exception.MqClientException;
//import com.meitun.mq.util.MqNameUtil;
//import com.meitun.mq.util.MqProductConnectionSingleton;
//import com.meitun.mq.util.MqSerializeUtil;
//import com.meitun.mq.util.MqQueueHaPolicySingleton;
//import com.rabbitmq.client.AMQP.BasicProperties;
//import com.rabbitmq.client.AMQP;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.ConfirmListener;
//import com.rabbitmq.client.MessageProperties;
//import com.rabbitmq.client.ReturnListener;
//
///**
// * @author longhaisheng
// *
// */
//public class CopyOfRabbitMqProducer {
//
//	private static final Log logger = LogFactory.getLog(CopyOfRabbitMqProducer.class);
//
//	private MqMessageConfigs messageConfigs;
//
//	public MqMessageConfigs getMessageConfigs() {
//		return messageConfigs;
//	}
//
//	public void setMessageConfigs(MqMessageConfigs messageConfigs) {
//		this.messageConfigs = messageConfigs;
//	}
//
//	/**
//	 * 发送点对点消息
//	 * 
//	 * @param oldQueueName
//	 *            队列名
//	 * @param t
//	 *            要发送的消息对象
//	 * @throws IOException
//	 */
//	public void sendP2PMessage(String oldQueueName, Object t) throws MqClientException {
//		if (null == t) {
//			String message = oldQueueName + " send message must not null ";
//			logger.error(message);
//			throw new MqClientException(message);
//		}
//		if (!(t instanceof Serializable)) {
//			String message = oldQueueName + " send message must instanceof Serializable interface ";
//			logger.error(message);
//			throw new MqClientException(message);
//		}
//		if (this.getMessageConfigs().existsP2p()) {
//			String message = oldQueueName + "sendP2PMessage 队列名配置重复";
//			logger.error(message);
//			throw new MqClientException(message);
//		}
//		if (null != oldQueueName && !"".equals(oldQueueName)) {
//			Channel channel = null;
//			boolean messageBurable = true;
//			BasicProperties baseProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
//			MqP2pConfig queueConfigDO = messageConfigs.getQueueConfigDO(oldQueueName);
//			if (!queueConfigDO.getSendMessageType().equalsIgnoreCase(MqConstants.SEND_MESSAGE_TYPE_P2P)) {
//				String message = "oldQueueName=>" + oldQueueName + "send message type must " + MqConstants.SEND_MESSAGE_TYPE_P2P;
//				logger.error(message);
//				throw new MqClientException(message);
//			}
//			String exchangeName = MqNameUtil.getExchangeName(oldQueueName);
//			String newQueueName = MqNameUtil.getQueueName(oldQueueName);
//			String routingKey = MqNameUtil.getRoutingKey(oldQueueName);
//			try {
//				System.out.println("=========+++++=========");
//				channel = MqProductConnectionSingleton.connection().createChannel();
//
//				if (!queueConfigDO.isQueueDurable()) {
//					messageBurable = false;
//					baseProp = null;
//				}
//				channel.confirmSelect();
//				channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);
//				channel.queueDeclare(newQueueName, messageBurable, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());
//				channel.queueBind(newQueueName, exchangeName, routingKey);
//
//				byte[] bytes = MqSerializeUtil.serialize(t);
//
//				// channel.basicPublish(exchangeName, routingKey, baseProp,
//				// bytes);
//
//				channel.addReturnListener(new ReturnListener() {
//					public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
//							AMQP.BasicProperties properties, byte[] body) throws IOException {
//						System.out.println("-----------get exception reply message---------");
//						System.out.println("replyCode: " + replyCode);
//						System.out.println("replyText: " + replyText);
//						System.out.println("exchange: " + exchange);
//						System.out.println("routingKey: " + routingKey);
//						System.out.println("ContentEncoding: " + properties.getContentEncoding());
//						System.out.println("content type: " + properties.getContentType());
//						System.out.println("Expiration: " + properties.getExpiration());
//						System.out.println("type: " + properties.getType());
//						System.out.println("reply to: " + properties.getReplyTo());
//						System.out.println("body: " + new String(body));
//					}
//				});
//				channel.addConfirmListener(new ConfirmListener() {
//					public void handleAck(long deliveryTag, boolean multiple) throws IOException {
//						System.out.println("====>" + deliveryTag + "multiple=>" + multiple);
//					}
//
//					public void handleNack(long deliveryTag, boolean multiple) throws IOException {
//						System.out.println("handleNack");
//
//					}
//				});
//				channel.basicPublish(exchangeName, routingKey, true, false, baseProp, bytes);
//			} catch (Exception ex) {
//				System.out.println("eeeeeeeeee");
//				String message = "oldQueueName=>" + oldQueueName + "send message exception for:" + ex.getMessage();
//				logger.error(message);
//				throw new MqClientException(message);
//
//			} finally {
//				try {
//					if (channel != null) {
//						channel.close();
//					}
//				} catch (Exception ex) {
//					String message = "queueName=" + oldQueueName + "channel close exception for " + ex.getMessage();
//					logger.error(message);
//					throw new MqClientException(message);
//				}
//			}
//		}
//	}
//
//	/**
//	 * 发送主题订阅消息 不支持持久化
//	 * 
//	 * @param queueName
//	 *            队列名
//	 * @param t
//	 *            要发送的消息
//	 * @throws IOException
//	 */
//	public void sendTopicMessage(String queueName, Object t) throws MqClientException {
//		if (null == t) {
//			String message = queueName + " send message must not null ";
//			logger.error(message);
//			throw new MqClientException(message);
//		}
//		if (null != queueName && !"".equals(queueName)) {
//			if (!(t instanceof Serializable)) {
//				String message = queueName + " send message must instanceof Serializable interface ";
//				logger.error(message);
//				throw new MqClientException(message);
//			}
//			if (this.getMessageConfigs().existsTopic()) {
//				logger.error("sendTopicMessage 队列名配置重复");
//				throw new MqClientException("sendTopicMessage 队列名配置重复");
//			}
//
//			MqTopicConfig topicConfigDO = messageConfigs.getTopicConfigDO(queueName);
//			if (null != topicConfigDO && !topicConfigDO.getSendMessageType().equalsIgnoreCase(MqConstants.SEND_MESSAGE_TYPE_PUB_SUB)) {
//				String message = "oldQueueName=>" + queueName + "send message type must " + MqConstants.SEND_MESSAGE_TYPE_PUB_SUB;
//				logger.error(message);
//				throw new MqClientException(message);
//			}
//
//			String exchangeName = MqNameUtil.getExchangeName(queueName);
//			Channel channel = null;
//			try {
//				channel = MqProductConnectionSingleton.connection().createChannel();
//				channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_FANOUT, true);
//
//				byte[] bytes = MqSerializeUtil.serialize(t);
//				channel.basicPublish(exchangeName, "", MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
//			} catch (Exception ex) {
//				String message = "sendTopicMessage method queueName = " + queueName + " ;exchangeName=" + exchangeName + ",exception for "
//						+ ex.getMessage();
//				logger.error(message);
//				throw new MqClientException(message);
//			} finally {
//				try {
//					if (channel != null) {
//						channel.close();
//					}
//				} catch (Exception ex) {
//					String message = "sendTopicMessage method  queueName = " + queueName + ",channel close exception for "
//							+ ex.getMessage();
//					logger.error(message);
//					throw new MqClientException(message);
//				}
//			}
//		}
//
//	}
//}
