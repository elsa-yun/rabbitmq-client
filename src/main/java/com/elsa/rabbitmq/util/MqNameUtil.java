package com.elsa.rabbitmq.util;

/**
 * @author longhaisheng
 *
 */
public class MqNameUtil {

	public final static String getExchangeName(String queueName) {
		return "exchange-" + queueName;
	}

	public final static String getQueueName(String queueName) {
		return "queue-" + queueName;
	}

	public final static String getRoutingKey(String queueName) {
		return "key-" + queueName;
	}

}