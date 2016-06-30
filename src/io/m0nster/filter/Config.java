package io.m0nster.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalTime;
import java.util.Properties;

/**
 * @author PointerRage
 *
 */
public class Config {
	public static void load(File file) throws UncheckedIOException {
		Properties p = new Properties();
		try (FileInputStream fis = new FileInputStream(file)) {
			p.load(fis);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		
		gameserverAddress = p.getProperty("filter.game.address", "78.46.107.153");
		gameserverPort = Integer.parseInt(p.getProperty("filter.game.port", "7777"));
		
		filterAddress = p.getProperty("filter.address", "37.204.241.175");
		filterPort = Integer.parseInt(p.getProperty("filter.port", "9000"));
		
		readWriteThreads = Integer.parseInt(p.getProperty("filter.worker.readwrite.threads", "1"));
		
		restartTime = LocalTime.parse(p.getProperty("filter.restart.time", "6:00"));
	}
	
	private static String gameserverAddress;
	private static int gameserverPort;
	
	private static String filterAddress;
	private static int filterPort;
	
	private static int readWriteThreads;
	
	private static LocalTime restartTime;
	
	public static String getGameserverAddress() {
		return gameserverAddress;
	}
	
	public static int getGameserverPort() {
		return gameserverPort;
	}
	
	public static String getFilterAddress() {
		return filterAddress;
	}
	
	public static int getFilterPort() {
		return filterPort;
	}
	
	public static int getReadWriteThreads() {
		return readWriteThreads;
	}
	
	public static LocalTime getRestartTime() {
		return restartTime;
	}
}
