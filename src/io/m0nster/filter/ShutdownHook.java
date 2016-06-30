package io.m0nster.filter;

import java.util.concurrent.TimeUnit;

/**
 * @author PointerRage
 *
 */
public class ShutdownHook extends Thread {
	public ShutdownHook() {
		setName("Shutdown");
	}
	
	@Override
	public void run() {
		try {
			Starter.getExecutor().awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		WorkerManager.getInstance().close();
	}
}
