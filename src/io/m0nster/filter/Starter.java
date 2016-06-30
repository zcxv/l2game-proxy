package io.m0nster.filter;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author PointerRage
 *
 */
public class Starter {
	private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	public static void main(String...args) {
		Config.load(new File("config.properties"));
		Thread.setDefaultUncaughtExceptionHandler(new ThrowCatcher());
		
		WorkerManager manager = WorkerManager.getInstance();
		manager.setupReadWriteWorkers(Config.getReadWriteThreads());
		manager.startup();
		manager.getAcceptCloseWorker().register(
			openServer(Config.getFilterAddress(), Config.getFilterPort()), 
			SelectionKey.OP_ACCEPT, 
			null
		);
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		
		LocalDateTime scheduled = LocalDateTime.of(LocalDate.now(), Config.getRestartTime());
		if(scheduled.isBefore(LocalDateTime.now())) {
			scheduled = scheduled.plusDays(1);
		}
		
		executor.schedule(new AutoRestart(), scheduled.atZone(ZoneOffset.systemDefault()).toEpochSecond(), TimeUnit.SECONDS);
	}
	
	private static ServerSocketChannel openServer(String address, int port) throws UncheckedIOException {
		try {
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.bind(new InetSocketAddress(address, port), 2000);
			return channel;
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static ScheduledExecutorService getExecutor() {
		return executor;
	}
}
