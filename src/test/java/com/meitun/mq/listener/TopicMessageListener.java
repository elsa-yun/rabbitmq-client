package com.meitun.mq.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meitun.mq.thread.PubSubMessage;

/**
 * 主题订阅(pub_sub)消息 web监听器
 * 
 * @author longhaisheng
 *
 */
public class TopicMessageListener implements ServletContextListener {

	private static final Log logger = LogFactory.getLog(TopicMessageListener.class);

	private static final String CHAR = "+++++++++++++++";

	private ConcurrentMap<String, PubSubMessage> topicMessageMap = new ConcurrentHashMap<String, PubSubMessage>();

	private ConcurrentMap<String, Thread> threadMap = new ConcurrentHashMap<String, Thread>();

	public void contextInitialized(ServletContextEvent sce) {
		PubSubMessage pubSub = new PubSubMessage();
		Thread t = new Thread(pubSub);
		t.setDaemon(true);
		String name = "topic-test-new-thread-0";
		t.setName(name);
		topicMessageMap.put(name, pubSub);
		threadMap.put(name, t);
		t.start();

	}

	public void contextDestroyed(ServletContextEvent sce) {
		for (Map.Entry<String, PubSubMessage> entry : topicMessageMap.entrySet()) {
			PubSubMessage p2p = entry.getValue();
			p2p.stopRequest();//手动停止线程运行
		}
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e1) {
			logger.error("topic ConsumerConnectionSingleton.destoryall exception for " + e1.getMessage());
		}
		for (Map.Entry<String, Thread> entry : threadMap.entrySet()) {
			Thread t = entry.getValue();
			try {
				if (null != t) {
					t.interrupt();//再中断一次
				}
			} catch (SecurityException e) {
				logger.error(CHAR + "thread name " + t.getName() + "interrupt exception for " + e.getMessage() + CHAR);
			}
		}

		try {
			TimeUnit.MILLISECONDS.sleep(200);
		} catch (InterruptedException e1) {
			logger.error(CHAR + "topic- ConsumerConnectionSingleton.destoryall exception for " + e1.getMessage() + CHAR);
		}

		for (Map.Entry<String, Thread> entry : threadMap.entrySet()) {
			threadMap.remove(entry.getKey());
		}

	}

}