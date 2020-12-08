package com.perf.test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.elsa.rabbitmq.SendMessageFailCallBack;
import com.elsa.rabbitmq.domain.MqConstants;
import com.elsa.rabbitmq.domain.MqMessageConfigs;
import com.elsa.rabbitmq.util.MqProductConnectionSingleton;
import com.elsa.rabbitmq.util.MqQueueHaPolicySingleton;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;

public class AsyncConfirmSender extends Sender {

	private final static Map<Channel, SortedSet<Long>> map = new ConcurrentHashMap<Channel, SortedSet<Long>>();
	
	private MqMessageConfigs messageConfigs;

	private SendMessageFailCallBack sendMessageFailCallBack;

	public MqMessageConfigs getMessageConfigs() {
		return messageConfigs;
	}

	public void setMessageConfigs(MqMessageConfigs messageConfigs) {
		this.messageConfigs = messageConfigs;
	}

	public static void main(String[] argv) throws Exception {
		AsyncConfirmSender acs = new AsyncConfirmSender();
		acs.sendMsg(getMsg());
	}

	public void sendMsg(final String msg) {
		System.out.println(messageConfigs);
		final CountDownLatch msgcountLatch = new CountDownLatch(msgcount);
//		final CountDownLatch producerLatch = new CountDownLatch(producerCount);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < producerCount; i++) {
			// new Thread(new Runnable() {
			// public void run() {
			try {
//				final Channel channel = createConfirmChannel();
				final Channel channel =  MqProductConnectionSingleton.connection().createChannel();
				channel.confirmSelect();
				map.put(channel, Collections.synchronizedSortedSet(new TreeSet<Long>()));
				channel.addConfirmListener(new ConfirmListener() { // called by
					public void handleNack(long deliveryTag, boolean multiple) throws IOException {
						System.out.println("Nack, SeqNo: " + deliveryTag + ", multiple: " + multiple + " msg=>" + msg);
						if (multiple) {
							map.get(channel).headSet(deliveryTag + 1).clear();
						} else {
							map.get(channel).remove(deliveryTag);
						}
					}

					public void handleAck(long deliveryTag, boolean multiple) throws IOException {
						System.out.println("aaaa=====YYYYYYYYYY===Ack, SeqNo: " + deliveryTag + ", multiple: " + multiple + " msg=>" + msg);
						if (multiple) {
							map.get(channel).headSet(deliveryTag + 1).clear();
						} else {
							map.get(channel).remove(deliveryTag);
						}
					}
				});
				while (msgcountLatch.getCount() > 0) {
					msgcountLatch.countDown();
					long nextSeqNo = channel.getNextPublishSeqNo();
					// channel.basicPublish("", QUEUE_NAME, prop,
					// message.getBytes());
					String exchangeName = "confirm_test_exchange";
					String newQueueName = "confirm_test_queue";
					String routingKey = "confirm_test_routingKey";
					BasicProperties baseProp = MessageProperties.PERSISTENT_TEXT_PLAIN;
					boolean messageBurable = true;
					channel.exchangeDeclare(exchangeName, MqConstants.EXCHANGE_TYPE_DIRECT, true);
					channel.queueDeclare(newQueueName, messageBurable, false, false, MqQueueHaPolicySingleton.getInstance().getQueueArgs());
					channel.queueBind(newQueueName, exchangeName, routingKey);
					channel.basicPublish(exchangeName, routingKey, baseProp, msg.getBytes());
					map.get(channel).add(nextSeqNo);
				}
//				while (!map.get(channel).isEmpty()) {
					// System.out.println("unconfirmedSet size: " +
					// unconfirmedSet.size());
//				}
				// producerLatch.countDown();
				closeChannel(channel);
			} catch (Exception ignore) {
			}
		}
		// }).start();
		// }
		// producerLatch.await();
		long elapseTime = System.currentTimeMillis() - startTime;
		System.out.println("00Confirm Type: async confirm.");
		System.out.println("Message Size: " + msglen + " Bytes");
		System.out.println("Message Count: " + msgcount);
		System.out.println("Message Persistent: " + (prop.getDeliveryMode() == 2 ? "true" : "false"));
		System.out.println("Consumer Autoack: " + (autoAck ? "true" : "false"));
		System.out.println("Consumer Count: " + consumerCount);
		System.out.println("Producer Count: " + producerCount);
		System.out.println("Elapse Time: " + elapseTime / 1000 + " s");
		System.out.println("Publish Rate: " + msgcount * 1000 / elapseTime + " msg/s");
	}
}
