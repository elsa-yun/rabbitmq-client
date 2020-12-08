package com.meitun.mq.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsa.rabbitmq.domain.MqConstants;
import com.elsa.rabbitmq.util.MqApplicationContextBeans;
import com.elsa.rabbitmq.util.MqConsumerConnectionSingleton;
import com.elsa.rabbitmq.util.MqNameUtil;
import com.elsa.rabbitmq.util.MqSerializeUtil;
import com.meitun.mq.bean.test.OrderAO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * (pub_sub)主题订阅消息 接收对象
 * 
 * @author longhaisheng
 *
 */
public class PubSubMessage implements Runnable {

	private static final Log logger = LogFactory.getLog(PubSubMessage.class);

	private final static String TOPIC_TASK_QUEUE_NAME = "order_task_queue_topic_1";

	private static OrderAO orderAO;

	private volatile boolean stopRequested = false;

	static {
		orderAO = (OrderAO) MqApplicationContextBeans.getBean("orderAO");
	}

	public void run() {
		boolean autoAck = false;// 是否自动应答消息 true为自动应答消息，false为不自动应答
		Channel channel = null;
		try {
			channel = null;// MqConsumerConnectionSingleton.connection().createChannel();
			String exchangeName = MqNameUtil.getExchangeName(TOPIC_TASK_QUEUE_NAME);//
			channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_FANOUT, true);

			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, exchangeName, "");
			channel.basicQos(1);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, autoAck, consumer);
			while (!stopRequested) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				if (null != delivery) {
					byte[] bytes = delivery.getBody();
					String message = MqSerializeUtil.unserialize(bytes).toString();
					if (logger.isDebugEnabled()) {
						logger.debug("topic queue name=>" + TOPIC_TASK_QUEUE_NAME + " recive msg::::::=>" + message + " orderAO get name "
								+ "=>" + orderAO.getName());
					}
					if (!autoAck) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					}
				}
			}
		} catch (Exception e) {
			logger.error("close msg is " + e.getMessage());
		} finally {
			try {
				if (channel != null) {
					channel.close();
				}
			} catch (Exception ex) {
				logger.error("channel close exception for " + ex.getMessage());
			}
		}

	}

	public void stopRequest() {
		stopRequested = true;
	}

}
