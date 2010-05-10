package com.lazerbank;

import java.util.concurrent.ThreadFactory;

public class LazerBankThreadFactory implements ThreadFactory {

	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}

}
