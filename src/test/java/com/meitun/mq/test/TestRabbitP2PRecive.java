//package com.meitun.mq.test;
//
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.meitun.mq.RabbitMqProduct;
//import com.meitun.mq.domain.MqConstants;
//import com.meitun.mq.util.MqConnectionUtil;
//import com.meitun.mq.util.MqNameUtil;
//import com.meitun.mq.util.MqSerializeUtil;
//import com.meitun.mq.util.MqQueueHaPolicySingleton;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.QueueingConsumer;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath*:spring.xml" })
//public class TestRabbitP2PRecive {
//
//	private final static String TASK_QUEUE_NAME = "task_queue_p2p";
//
//	@Autowired
//	RabbitMqProduct rabbitMqProduct;
//
//	public static void testRevicePubSub() throws InterruptedException {
//		Thread thread = new Thread(new Runnable() {
//			Connection connection = null;
//			Channel channel = null;
//
//			public void run() {
//				try {
//					connection = MqConnectionUtil.getConnection();
//
//					Channel channel = connection.createChannel();
//					String queueName = TASK_QUEUE_NAME;
//
//					String exchangeName = MqNameUtil.getExchangeName(queueName);
//					String newQueueName = MqNameUtil.getQueueName(queueName);
//					String routingKey = MqNameUtil.getRoutingKey(queueName);
//
//					channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);
//					channel.queueDeclare(newQueueName, true, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());
//					channel.queueBind(newQueueName, exchangeName, routingKey);
//
//					System.out.println(" ============[*] Waiting for messages. To exit press CTRL+C");
//
//					channel.basicQos(1);
//
//					QueueingConsumer consumer = new QueueingConsumer(channel);
//					channel.basicConsume(newQueueName, false, consumer);
//
//					while (true) {
//						QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//						if (null != delivery) {
//							System.out.println("exchange =>" + delivery.getEnvelope().getExchange() + " key=>"
//									+ delivery.getEnvelope().getRoutingKey());
//							byte[] bytes = delivery.getBody();
//							if (null != bytes) {
//								String message = MqSerializeUtil.unserialize(bytes).toString();
//								// TODO 业务处理 
//								System.out.println("P2PRecive Received => '" + message + "'");
//								doWork(message);
//								System.out.println(" [x] Done");
//							}
//							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
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
//	private static void doWork(String task) throws InterruptedException {
//		for (char ch : task.toCharArray()) {
//			if (ch == '.')
//				Thread.sleep(1000);
//		}
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
