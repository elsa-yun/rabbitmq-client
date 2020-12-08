//package com.meitun.mq.test;
//
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.meitun.mq.RabbitMqProduct;
//import com.meitun.mq.domain.MqConstants;
//import com.meitun.mq.test.topic.TC1;
//import com.meitun.mq.util.MqConnectionUtil;
//import com.meitun.mq.util.MqNameUtil;
//import com.meitun.mq.util.MqSerializeUtil;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.QueueingConsumer;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath*:spring.xml" })
//public class TestRabbitTopicRecive {
//
//	private final static String TASK_QUEUE_NAME = "task_queue_p2p";
//
//	private final static String TOPIC_TASK_QUEUE_NAME = "task_queue_topic_1";
//
//	@Autowired
//	RabbitMqProduct rabbitMqProduct;
//
//	public static void testRevicePubSub() throws InterruptedException {
//		Thread thread = new Thread(new Runnable() {
//			public void run() {
//				Connection connection = null;
//				Channel channel = null;
//				try {
//					connection = MqConnectionUtil.getConnection();
//					channel = connection.createChannel();
//					String exchangeName = MqNameUtil.getExchangeName(TOPIC_TASK_QUEUE_NAME);
//					channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_FANOUT, true);
//
//					String queueName = channel.queueDeclare().getQueue();
//					channel.queueBind(queueName, exchangeName, "");
//					System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//					QueueingConsumer consumer = new QueueingConsumer(channel);
//					channel.basicConsume(queueName, true, consumer);
//					while (true) {
//						QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//						if (null != delivery) {
//							String message = MqSerializeUtil.unserialize(delivery.getBody()).toString();
//							//TODO 业务处理
//							System.out.println(TC1.class + ": [x] Received '" + message + "'");
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					try {
//						if (channel != null) {
//							channel.close();
//						}
//						if (connection != null) {
//							connection.close();
//						}
//					} catch (Exception ex) {
//
//					}
//				}
//			}
//		});
//
//		thread.start();
//	}
//
//	public static void main(String args[]) throws InterruptedException {
//		testRevicePubSub();
//		testRevicePubSub();
//	}
//
//	private static String getTopicMessage() {
//		return "topic message Hello World========!";
//	}
//
//	private static String getP2pMessage() {
//		return "ggvvvvvvvvgggg Hello World========!";
//	}
//
//}
