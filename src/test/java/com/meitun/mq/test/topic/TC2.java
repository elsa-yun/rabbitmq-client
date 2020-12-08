package com.meitun.mq.test.topic;

import java.io.File;
import java.io.IOException;

import com.elsa.rabbitmq.MqMessageCallBack;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class TC2 {

	private static final String E_SOFT = "E://";

	private static boolean createFile() throws IOException {
		boolean flag = false;
		String filenameTemp = E_SOFT + System.currentTimeMillis() + ".txt";
		File filename = new File(filenameTemp);
		if (!filename.exists()) {
			filename.createNewFile();
			flag = true;
		}
		return flag;
	}

	private static final class A implements MqMessageCallBack {
		public boolean execute(Object o) {
			System.out.println("recive msg is :" + o);
			try {
				createFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
	}

	private static final String EXCHANGE_NAME = TP.EXCHANGE_NAME;

	public static void main(String[] args) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {

	}

}
