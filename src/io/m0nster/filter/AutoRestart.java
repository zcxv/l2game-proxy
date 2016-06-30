package io.m0nster.filter;

/**
 * @author PointerRage
 *
 */
public class AutoRestart implements Runnable {

	@Override
	public void run() {
		System.exit(1);
	}

}
