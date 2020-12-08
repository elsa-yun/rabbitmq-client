package com.elsa.rabbitmq.pref;

import java.util.concurrent.LinkedBlockingQueue;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class Recver extends MQConfig {

	public static void main(String[] argv) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		Address[] addrArr = getAddressArray();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setRequestedHeartbeat(10);
		factory.setAutomaticRecoveryEnabled(true);
		factory.setNetworkRecoveryInterval(5000);
		final Connection connection = factory.newConnection(addrArr);

		for (int i = 0; i < consumerCount; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Channel channel = connection.createChannel();
						channel.queueDeclare("confirm_test_queue", true, false, false, null);
						channel.basicQos(1);
						LinkedBlockingQueue<QueueingConsumer.Delivery> blockingQueue = new LinkedBlockingQueue<QueueingConsumer.Delivery>();
						QueueingConsumer consumer = new QueueingConsumer(channel, blockingQueue);
						// QueueingConsumer consumer = new
						// QueueingConsumer(channel);
						channel.basicConsume("confirm_test_queue", autoAck, consumer);
						System.out.println(" [*] Waiting for messages.");

						while (true) {
							QueueingConsumer.Delivery delivery = consumer.nextDelivery();
							String message = new String(delivery.getBody());
							if (!autoAck) {
								System.out.println(" [x] Received '" + message + "'" + "  ack is ====>true");
								channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
							}
						}
					} catch (Exception ignore) {
					}
				}
			}).start();
		}

		while (true) {
			Thread.sleep(1000);
		}
		// connection.close();
	}
}
