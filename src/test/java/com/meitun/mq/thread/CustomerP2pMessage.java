package com.meitun.mq.thread;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsa.rabbitmq.RabbitMqP2pConsumer;
import com.elsa.rabbitmq.util.MqApplicationContextBeans;
import com.elsa.rabbitmq.util.MqNameUtil;
import com.elsa.rabbitmq.util.MqSerializeUtil;
import com.meitun.mq.bean.test.OrderAO;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * P2P点对点消息 接收对象
 * 
 * @author longhaisheng
 *
 */
public class CustomerP2pMessage implements Runnable {

	private static final Log logger = LogFactory.getLog(CustomerP2pMessage.class);

	private final static String TASK_QUEUE_NAME = "order_task_queue_p2p";

	private volatile boolean stopRequested = false;

	private static OrderAO orderAO;

	private static RabbitMqP2pConsumer rabbitMqP2pConsumer;

	static {
		orderAO = (OrderAO) MqApplicationContextBeans.getBean("orderAO");
		rabbitMqP2pConsumer = (RabbitMqP2pConsumer) MqApplicationContextBeans.getBean("rabbitMqP2pConsumer");
	}

	public void run() {
		// String exchangeName = MqNameUtil.getExchangeName(TASK_QUEUE_NAME);//原始方式
		// String newQueueName = MqNameUtil.getQueueName(TASK_QUEUE_NAME);//原始方式
		// String routingKey = MqNameUtil.getRoutingKey(TASK_QUEUE_NAME);//原始方式

		boolean autoAck = false;//是否自动应答消息 true为自动应答消息，false为不自动应答
		boolean exceptionflag = false;
		Channel channel = null;
		while (!stopRequested) {
			try {
				// channel =
				// MqConsumerConnectionSingleton.connection().createChannel();//原始方式
				// channel.exchangeDeclare(exchangeName,
				// MqConstants.EXCHANGE_TYPE_DIRECT, true);//原始方式 
				// channel.queueDeclare(newQueueName, true, false, false,
				// MqQueueHaPolicySingleton.getInstance().getQueueArgs());//原始方式
				// channel.queueBind(newQueueName, exchangeName, routingKey);//原始方式
				// 

				channel = rabbitMqP2pConsumer.getP2pChannel(TASK_QUEUE_NAME);
				String newQueueName = MqNameUtil.getQueueName(TASK_QUEUE_NAME);

				if (null != channel) {
					channel.basicQos(1);
					QueueingConsumer consumer = new QueueingConsumer(channel);
					channel.basicConsume(newQueueName, autoAck, consumer);

					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					if (null != delivery) {
						byte[] bytes = delivery.getBody();
						if (null != bytes) {
							String message = MqSerializeUtil.unserialize(bytes).toString();
							String routingKey2 = delivery.getEnvelope().getRoutingKey();
							String exchange = delivery.getEnvelope().getExchange();
							String data = "exchange =>" + exchange + " key=>" + routingKey2 + "msg =>" + message + "order getName=>"
									+ orderAO.getName();
							// TODO 业务处理
							if (logger.isDebugEnabled()) {
								logger.debug("pe2p recive msg =>" + data);
							}
						}
						if (!autoAck) {
							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
						}
					}
				}
			} catch (Exception ex) {
				exceptionflag = true;
				logger.error(ex.toString());
			} finally {
				try {
					if (channel != null) {
						channel.close();
					}
				} catch (Exception e) {
					logger.error(e.toString());
				}
				if (exceptionflag) {
					try {
						TimeUnit.MILLISECONDS.sleep(1000);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				}
			}
		}
	}

	public void stopRequest() {
		stopRequested = true;
	}

}
