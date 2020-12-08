package com.meitun.mq.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.meitun.mq.thread.CustomerP2pMessage;

/**
 * p2p点对点消息 web监听器
 * 
 * @author longhaisheng
 *
 */
public class P2pMessageListener implements ServletContextListener {

	private static final Log logger = LogFactory.getLog(P2pMessageListener.class);

	private static final String CHAR = "---------------";

	private ConcurrentMap<String, CustomerP2pMessage> p2pMessageMap = new ConcurrentHashMap<String, CustomerP2pMessage>();

	private ConcurrentMap<String, Thread> threadMap = new ConcurrentHashMap<String, Thread>();

	public void contextInitialized(ServletContextEvent sce) {
//		int count = MqBaseConfig.getBaseConfig().getConsumerThreadCount();
		CustomerP2pMessage p2p = new CustomerP2pMessage();
		int i = 0;
		Thread t = new Thread(p2p);
		t.setDaemon(true);
		String name = "p2p-test-new-thread-" + i;
		t.setName(name);
		p2pMessageMap.put(name, p2p);
		threadMap.put(name, t);
		t.start();
	}

	public void contextDestroyed(ServletContextEvent context) {
		for (Map.Entry<String, CustomerP2pMessage> entry : p2pMessageMap.entrySet()) {
			CustomerP2pMessage p2p = entry.getValue();
			p2p.stopRequest();//手动停止线程运行
		}
		try {
			TimeUnit.MILLISECONDS.sleep(50);
		} catch (InterruptedException e1) {
			logger.error(CHAR + "P2P ConsumerConnectionSingleton.destoryall exception for " + e1.getMessage() + CHAR);
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
			logger.error("P2P ConsumerConnectionSingleton.destoryall exception for " + e1.getMessage());
		}

		for (Map.Entry<String, Thread> entry : threadMap.entrySet()) {
			threadMap.remove(entry.getKey());
		}

	}

}