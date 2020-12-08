package com.meitun.mq.test.topic;

import java.util.HashMap;
import java.util.Map;

import com.elsa.rabbitmq.RabbitMqProducer;
import com.elsa.rabbitmq.domain.MqMessageConfigs;
import com.elsa.rabbitmq.domain.MqTopicConfig;
import com.elsa.rabbitmq.util.MqProductConnectionSingleton;

public class TP {

	public static final String EXCHANGE_NAME = "task_queue_topic_1";

	public static void main(String[] argv) throws Exception {

		// Connection connection = MQConnectionUtil.getConnection();
		// Channel channel = connection.createChannel();
		//
		// channel.exchangeDeclare(EXCHANGE_NAME, "fanout",true);
		//
		// String message = getMessage(argv);
		//
		// channel.basicPublish(EXCHANGE_NAME, "",
		// MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
		// System.out.println(" [x] Sent '" + message + "'");
		//
		// channel.close();
		// connection.close();

		MqMessageConfigs mqQueueConfigsDO = new MqMessageConfigs();
		Map<String, MqTopicConfig> topicConfigsMap = new HashMap<String, MqTopicConfig>();

		MqTopicConfig topicConfig = new MqTopicConfig();
		topicConfig.setExchangeName(EXCHANGE_NAME);
		topicConfigsMap.put(EXCHANGE_NAME, topicConfig);
		mqQueueConfigsDO.setTopicConfigsMap(topicConfigsMap);

		RabbitMqProducer product = new RabbitMqProducer();
		product.setMessageConfigs(mqQueueConfigsDO);

		String message = getMessage(argv);
		product.sendTopicMessage(EXCHANGE_NAME, message);
		MqProductConnectionSingleton.destoryall();

	}

	private static String getMessage(String[] strings) {
		if (strings.length < 1)
			return "info: Hello World!";
		return joinStrings(strings, " ");
	}

	private static String joinStrings(String[] strings, String delimiter) {
		int length = strings.length;
		if (length == 0)
			return "";
		StringBuilder words = new StringBuilder(strings[0]);
		for (int i = 1; i < length; i++) {
			words.append(delimiter).append(strings[i]);
		}
		return words.toString();
	}
}
