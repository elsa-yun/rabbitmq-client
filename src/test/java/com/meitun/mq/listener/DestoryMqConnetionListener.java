package com.meitun.mq.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsa.rabbitmq.util.MqConsumerConnectionSingleton;
import com.elsa.rabbitmq.util.MqProductConnectionSingleton;

/**
 * 关闭 mq 生产者和消费者的连接的listener
 * 
 * @author longhaisheng
 *
 */
public class DestoryMqConnetionListener implements ServletContextListener {

	private static final Log logger = LogFactory.getLog(DestoryMqConnetionListener.class);

	private static final String CHAR = "===================================";

	public void contextInitialized(ServletContextEvent sce) {
		logDebug(CHAR + "DestoryMqConnetionListener.class contextInitialized" + CHAR);

	}

	public void contextDestroyed(ServletContextEvent context) {
		try {
			MqConsumerConnectionSingleton.destoryall();
			this.logDebug(CHAR + "topic ConsumerConnectionSingleton.destoryall " + CHAR);
		} catch (Exception e) {
			logger.error(CHAR + "topic ConsumerConnectionSingleton.destoryall exception for " + e.getMessage() + CHAR);
		}
		try {
			MqProductConnectionSingleton.destoryall();
			this.logDebug(CHAR + "topic ProductConnectionSingleton.destoryall" + CHAR);
		} catch (Exception e) {
			logger.error(CHAR + "topic ProductConnectionSingleton.destoryall exception for" + CHAR);
		}
	}

	private void logDebug(String msg) {
		if (logger.isDebugEnabled()) {
			logger.debug(msg);
		}
	}
}