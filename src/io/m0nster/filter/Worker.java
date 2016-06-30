package io.m0nster.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PointerRage
 *
 */
public class Worker implements Runnable {
	private final static Logger log = LoggerFactory.getLogger(Worker.class);
	private final AbstractWorker worker;
	public Worker(AbstractWorker worker) {
		this.worker = worker;
	}
	
	@Override
	public void run() {
		for(;;) {
			try {
				worker.work();
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("Worker {} is interrupted!", Thread.currentThread().getName());
				return;
			} catch(Throwable e) {
				log.error("Worker {}", Thread.currentThread().getName(), e);
				break;
			}
		}
		
		try {
			worker.close();
		} catch(IOException e) {
			
		}
		log.warn("Worker {} shutdowned.", Thread.currentThread().getName());
	}

}
