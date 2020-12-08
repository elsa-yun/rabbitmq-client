package com.meitun.mq.test;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.elsa.rabbitmq.RabbitMqProducer;
import com.elsa.rabbitmq.exception.MqClientException;
import com.elsa.rabbitmq.util.MqProductConnectionSingleton;
import com.perf.test.AsyncConfirmSender;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring.xml" })
public class TestRabbitProducer {

	private final static String TASK_QUEUE_NAME = "confirm_msg_test";//order_task_queue_p2p

	private final static String TOPIC_TASK_QUEUE_NAME = "order_task_queue_topic_1";

	@Autowired
	RabbitMqProducer rabbitMqProducer;
	
	@Autowired
	AsyncConfirmSender asyncConfirmSender;

	@Test
	public void testP2p() throws InterruptedException {// 发送点对点消息
		for (int i = 0; i < 1; i++) {
			String message = getP2pMessage() + i;
			try {
				rabbitMqProducer.sendP2PMessage(TASK_QUEUE_NAME, message);
//				boolean sendSyncP2PMessage = rabbitMqProducer.sendSyncP2PMessage(TASK_QUEUE_NAME, message);
//				System.out.println(sendSyncP2PMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
//	@Test
//	public void testP2p() throws InterruptedException {// 发送点对点消息
//		AsyncConfirmSender as=new AsyncConfirmSender();
//		asyncConfirmSender.sendMsg("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
		
//	}

//	@Test
//	public void testPubSub() throws InterruptedException {// 发送主题 订阅消息
//		 String message = getTopicMessage();
//		 try {
//		 rabbitMqProducer.sendTopicMessage(TOPIC_TASK_QUEUE_NAME, message);
//		 } catch (MqClientException e) {
//		 e.printStackTrace();
//		 }
//		 System.out.println("-------");
//	}
//
//	@After
//	public void test() {
//		try {
//			System.out.println("start:::" + System.currentTimeMillis());
//			Thread.sleep(1000 * 1);
//			System.out.println("end:" + System.currentTimeMillis());
//			MqProductConnectionSingleton.destoryall();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println("----After running --------");
//	}

	private static String getTopicMessage() {
		return "rabbitmq-cient send topic message , hello world !";
	}

	private static String getP2pMessage() {//
		return "welcome china+++++++++++++++++++++++++";
	}

}
