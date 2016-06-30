package io.m0nster.filter;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @author PointerRage
 *
 */
public abstract class AbstractWorker implements Closeable {
	private final Selector selector;
	private int maxClients = 1000;
	
	public AbstractWorker() throws UncheckedIOException {
		try {
			selector = Selector.open();
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void setMaxClients(int maxClients) {
		this.maxClients = maxClients;
	}
	
	public int getMaxClients() {
		return maxClients;
	}
	
	public SelectionKey register(SelectableChannel channel, int ops, Client client) throws UncheckedIOException {
		try {
			return channel.register(selector, ops, client);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void work() throws InterruptedException {
		final long processStart = System.currentTimeMillis();
		
		int processed = work0();
		
		long sleepTime = System.currentTimeMillis() - processStart - maxClients + processed;
		if(sleepTime < 1) {
			Thread.yield();
		} else {
			try {
				Thread.sleep(sleepTime);
			} catch(InterruptedException e) {
				try {
					close();
				} catch(IOException e1) {}
				throw e;
			}
		}
	}
	protected abstract int work0();
	
	protected Selector getSelector() {
		return selector;
	}
	
	protected Client getClient(SelectionKey key) {
		return (Client) key.attachment();
	}
	
	//@Override
	@Override
	public void close() throws IOException {
		selector.close();
	}
}
