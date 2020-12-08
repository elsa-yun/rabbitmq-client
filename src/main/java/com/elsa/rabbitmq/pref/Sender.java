package com.elsa.rabbitmq.pref;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.elsa.rabbitmq.domain.MqBaseConfig;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Sender extends MQConfig {

	private static Connection connection = null;

	private static Set<Channel> cset = Collections.synchronizedSet(new HashSet<Channel>());

	private static void initConnection() throws Exception {
		if (connection == null) {
			ConnectionFactory factory = new ConnectionFactory();
			Address[] addrArr = getAddressArray();
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setRequestedHeartbeat(10);
			factory.setAutomaticRecoveryEnabled(true);
			factory.setNetworkRecoveryInterval(5000);
			connection = factory.newConnection(addrArr);
		}
	}

	protected static Channel createConfirmChannel() throws Exception {
		Channel channel = null;
		synchronized (Sender.class) {
			initConnection();
			channel = connection.createChannel();
		}
		// channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		// Channel channel =
		// MqProductConnectionSingleton.connection().createChannel();
		// channel.confirmSelect();
		cset.add(channel);
		return channel;
	}

	protected static void closeChannel(Channel c) {
		try {
			cset.remove(c);
			c.close();
			if (cset.isEmpty()) {
				connection.close();
			}
		} catch (Exception ignore) {
		}
	}

	protected static String getMsg() {
		return getMsg(msglen);
	}

	private static String getMsg(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append('a');
		}
		return sb.toString();
	}

}
