package io.m0nster.filter;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author PointerRage
 *
 */
public class ThrowCatcher implements UncaughtExceptionHandler {
	private final static Logger log = LoggerFactory.getLogger(ThrowCatcher.class);
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("{}", t.getName(), e);
	}
	
}
