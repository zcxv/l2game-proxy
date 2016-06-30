package io.m0nster.filter;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author PointerRage
 *
 */
public class WorkerManager implements Closeable {
	private static WorkerManager instance;
	public static WorkerManager getInstance() {
		return instance == null ? instance = new WorkerManager() : instance;
	}
	
	private final AcceptCloseWorker acceptCloseWorker = new AcceptCloseWorker();
	private final ConnectWorker connectWorker = new ConnectWorker();
	private final AtomicInteger readWriteCounter = new AtomicInteger();
	private ReadWriteWorker[] readWriteWorkers;
	private WorkerManager() {
		
	}
	
	public void startup() {
		Thread t = new Thread(new Worker(connectWorker));
		t.setName("ConnectWorker");
		t.start();
		
		t = new Thread(new Worker(acceptCloseWorker));
		t.setName("AcceptCloseWorker");
		t.start();
		
		for(int i = 0; i < readWriteWorkers.length; i++) {
			t = new Thread(new Worker(readWriteWorkers[i]));
			t.setName("ReadWriteWorker-" + i);
			t.start();
		}
	}
	
	public void setupReadWriteWorkers(int count) {
		if(readWriteWorkers != null) {
			throw new IllegalArgumentException();
		}
		
		readWriteWorkers = new ReadWriteWorker[count];
		for(int i = 0; i < count; i++) {
			readWriteWorkers[i] = new ReadWriteWorker();
		}
	}
	
	public AcceptCloseWorker getAcceptCloseWorker() {
		return acceptCloseWorker;
	}
	
	public ConnectWorker getConnectWorker() {
		return connectWorker;
	}
	
	public ReadWriteWorker getReadWriteWorker() {
		return readWriteWorkers[readWriteCounter.getAndIncrement() % readWriteWorkers.length];
	}
	
	@Override
	public void close() {
		try {
			acceptCloseWorker.close();
		} catch(IOException e) {}
		
		try {
			connectWorker.close();
		} catch(IOException e) {}
		
		for(int i = 0; i < readWriteWorkers.length; i++) {
			try {
				readWriteWorkers[i].close();
			} catch(IOException e) {
				
			}
		}
	}
}
