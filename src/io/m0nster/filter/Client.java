package io.m0nster.filter;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author PointerRage
 *
 */
public class Client implements Closeable {
	private final ConcurrentLinkedDeque<ByteBuffer> clientQueue = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<ByteBuffer> serverQueue = new ConcurrentLinkedDeque<>();
	private final AtomicBoolean closeLater = new AtomicBoolean(false);
	private final SelectionKey clientKey;
	private SelectionKey serverKey;
	public Client(SelectionKey clientKey) {
		this.clientKey = clientKey;
		try {
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			if(!channel.connect(new InetSocketAddress(Config.getGameserverAddress(), Config.getGameserverPort()))) {
				WorkerManager.getInstance().getConnectWorker().register(channel, SelectionKey.OP_CONNECT, this);
			} else {
				serverKey = WorkerManager.getInstance().getReadWriteWorker().register(channel, SelectionKey.OP_READ, this);
			}
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void putClient(ByteBuffer buffer) {
		if(closeLater.get()) {
			return;
		}
		clientQueue.offer(buffer);
	}
	
	public void putServer(ByteBuffer buffer) {
		if(closeLater.get()) {
			return;
		}
		serverQueue.offer(buffer);
	}
	
	public Deque<ByteBuffer> getClientQueue() {
		return clientQueue;
	}
	
	public Deque<ByteBuffer> getServerQueue() {
		return serverQueue;
	}
	
	public SelectionKey getClientKey() {
		return clientKey;
	}
	
	public SelectionKey getServerKey() {
		return serverKey;
	}
	
	public void setServerKey(SelectionKey serverKey) {
		this.serverKey = serverKey;
	}
	
	/** ex: server close connection or client close connection */
	public void closeLater() { 
		if(!closeLater.compareAndSet(false, true))
			return;
		
		if(!clientKey.channel().isOpen()) { //client close connection
			serverQueue.clear();
			if(serverKey != null) {
				serverKey.cancel();
			}
		} else { //server close connection
			clientQueue.clear();
			clientKey.cancel();
		}
		
		WorkerManager.getInstance().getAcceptCloseWorker().register(clientKey.channel(), SelectionKey.OP_CONNECT, this);
		if(serverKey != null) {
			WorkerManager.getInstance().getAcceptCloseWorker().register(serverKey.channel(), SelectionKey.OP_CONNECT, this);
		}
	}
	
	@Override
	public void close() {
		closeLater.set(true);
		
		clientQueue.clear();
		serverQueue.clear();
		
		clientKey.cancel();
		if(serverKey != null) {
			serverKey.cancel();
		}
	}
}
