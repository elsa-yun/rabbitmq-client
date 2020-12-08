package com.meitun.mq.test.topic;

import java.io.IOException;

import com.elsa.rabbitmq.util.MqNameUtil;
import com.elsa.rabbitmq.util.MqSerializeUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class TC1 {

	private static final String EXCHANGE_NAME = TP.EXCHANGE_NAME;

	public static final String ROUTING_KEY = "logs-key";

	public static void main(String[] args) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		Connection connection = null;// MqConnectionUtil.getConnection();
		Channel channel = connection.createChannel();

		String exchangeName = MqNameUtil.getExchangeName(EXCHANGE_NAME);
		channel.exchangeDeclare(exchangeName, "fanout", true);
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, exchangeName, "");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = MqSerializeUtil.unserialize(delivery.getBody()).toString();

			System.out.println(TC1.class + ": [x] Received '" + message + "'");
		}
	}

}
