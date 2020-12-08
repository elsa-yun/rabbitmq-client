//package com.meitun.mq.test;
//
//import java.util.concurrent.TimeUnit;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.meitun.mq.bean.ExecutorServiceTestNew;
//import com.meitun.mq.domain.MqConstants;
//import com.meitun.mq.util.MqApplicationContextBeans;
//import com.meitun.mq.util.MqConsumerConnectionSingleton;
//import com.meitun.mq.util.MqNameUtil;
//import com.meitun.mq.util.MqQueueHaPolicySingleton;
//import com.meitun.mq.util.MqSerializeUtil;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.QueueingConsumer;
//import com.meitun.mq.RabbitMqP2pConsumer;
//
///**
// * P2P��Ϣ ���տͻ���
// * 
// * @author longhaisheng
// *
// */
//public class CustomerP2pMessage implements Runnable {
//
//	public static final String B_TXT = "b.txt";
//
//	private static final Log logger = LogFactory.getLog(CustomerP2pMessage.class);
//
//	private final static String TASK_QUEUE_NAME = "task_queue_p2p";
//
//	private volatile boolean stopRequested = false;
//
//	private static OrderAO orderAO;
//
//	private static RabbitMqP2pConsumer rabbitMqP2pConsumer;
//
//	static {
//		orderAO = (OrderAO) MqApplicationContextBeans.getBean("orderAO");
//		rabbitMqP2pConsumer = (RabbitMqP2pConsumer) MqApplicationContextBeans.getBean("rabbitMqP2pConsumer");
//																												
//	}
//
//	@Override
//	public void run() {
////		String exchangeName = MqNameUtil.getExchangeName(TASK_QUEUE_NAME);// ԭʼ��ʽ
////		String newQueueName = MqNameUtil.getQueueName(TASK_QUEUE_NAME);// ԭʼ��ʽ
////		String routingKey = MqNameUtil.getRoutingKey(TASK_QUEUE_NAME);// ԭʼ��ʽ
//
//		boolean autoAck = false;// trueΪ�Զ�Ӧ����Ϣ��falseΪ���Զ�Ӧ��
//		boolean exceptionflag = false;
//		Channel channel = null;
//		while (!stopRequested) {
//			try {
////				channel = MqConsumerConnectionSingleton.connection().createChannel();// ԭʼ��ʽ
////				channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);// ԭʼ��ʽ
////				channel.queueDeclare(newQueueName, true, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());// ԭʼ��ʽ
////
////				channel.queueBind(newQueueName, exchangeName, routingKey);// ԭʼ��ʽ
//
//				channel = rabbitMqP2pConsumer.getP2pChannel(TASK_QUEUE_NAME);// ��װ���ṩ��ʽ
//				String newQueueName = MqNameUtil.getQueueName(TASK_QUEUE_NAME);// ��װ���ṩ��ʽ
//
//				if (null != channel) {
//					channel.basicQos(1);
//					QueueingConsumer consumer = new QueueingConsumer(channel);
//					channel.basicConsume(newQueueName, autoAck, consumer);
//
//					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//					if (null != delivery) {
//						byte[] bytes = delivery.getBody();
//						if (null != bytes) {
//							String message = MqSerializeUtil.unserialize(bytes).toString();
//							String routingKey2 = delivery.getEnvelope().getRoutingKey();
//							String exchange = delivery.getEnvelope().getExchange();
//							String data = "exchange =>" + exchange + " key=>" + routingKey2 + "msg =>" + message + "order getName=>"
//									+ orderAO.getName();
//							CustomerPubSubMessage.write(data, B_TXT);
//							// TODO ҵ����
//							if(logger.isDebugEnabled()){
//								logger.debug("================================================================");
//							}
//						}
//						if (!autoAck) {
//							channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//						}
//					}
//				}
//			} catch (Exception ex) {
//				exceptionflag = true;
//				logger.error(ex.toString());
//			} finally {
//				try {
//					if (channel != null) {
//						channel.close();
//					}
//				} catch (Exception e) {
//					logger.error(e.toString());
//				}
//				if (exceptionflag) {
//					try {
//						TimeUnit.MILLISECONDS.sleep(1000);
//					} catch (Exception e) {
//						logger.error(e.toString());
//					}
//				}
//			}
//		}
//	}
//
//	public void stopRequest() {
//		stopRequested = true;
//	}
//	
//}
