package com.meitun.mq.test.thread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.elsa.rabbitmq.MqMessageCallBack;

public class CallBackImpl implements MqMessageCallBack {

	public boolean execute(Object o) {
		try {
			write(o.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static final String E_SOFT = "E://";

	private static File createFile() throws IOException {
		boolean flag = false;
		String filenameTemp = E_SOFT + "a.txt";
		File filename = new File(filenameTemp);
		if (!filename.exists()) {
			filename.createNewFile();
			flag = true;
		}
		return filename;
	}

	public static void write(String data) throws IOException {

		File file = new File(E_SOFT + "a.txt");
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(data);
		bw.newLine();
		bw.close();

		System.out.println("Done");

	}

	public static void main(String args[]) throws IOException {
		String msg = "=aaaa===========";
		write(msg);
	}

}
