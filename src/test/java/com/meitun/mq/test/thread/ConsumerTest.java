package com.meitun.mq.test.thread;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsa.rabbitmq.MqMessageCallBack;
import com.elsa.rabbitmq.RabbitMqP2pConsumer;
import com.elsa.rabbitmq.util.MqNameUtil;
import com.elsa.rabbitmq.util.MqSerializeUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ConsumerTest implements Runnable {
	private static final Log logger = LogFactory.getLog(ConsumerTest.class);

	public static final String TASK_QUEUE_NAME = "task_queue_p2p";

	private String queue;

	private RabbitMqP2pConsumer customer;

	private MqMessageCallBack call;

	public ConsumerTest(String queue, RabbitMqP2pConsumer customer, MqMessageCallBack call) {
		this.queue = queue;
		this.customer = customer;
		this.call = call;

	}

	public void run() {
		System.out.println("=============");
		boolean exceptionflag = false;
		boolean autoAck = false;// 需要进行消息消费确认，关闭自动确认功能
		while (true) {
			System.out.println("=============");
			Channel channel = null;
			try {
				System.out.println("====ooooo=========");
				Integer i = 10;
				i++;
				System.out.println("====333=========" + i);
				channel =null;// this.customer.getP2PChannel(this.queue);
				channel.basicQos(1);
				String newQueueName = MqNameUtil.getQueueName(this.queue);
				QueueingConsumer consumer = new QueueingConsumer(channel);
				channel.basicConsume(newQueueName, autoAck, consumer);
				System.out.println("=============" + newQueueName);
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				if (delivery != null) {
					System.out.println("===vvv==========");
					byte[] tempByte = delivery.getBody();

					String message = MqSerializeUtil.unserialize(tempByte).toString();
					this.call.execute(message);
					System.out.println("recive msg is:" + message);

					if (!autoAck) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					}
				}
			} catch (Exception ex) {
				System.out.println("====kkkkkkk=========");
				exceptionflag = true;
				System.out.println(ex.toString());
			} finally {
				System.out.println("====finnally=========");

				try {
					if (channel != null) {
						channel.close();
					}
				} catch (Exception e) {
					logger.error(e.toString());
				}
				if (exceptionflag) {
					try {
						TimeUnit.MILLISECONDS.sleep(2000);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				}
			}
		}
	}
}
