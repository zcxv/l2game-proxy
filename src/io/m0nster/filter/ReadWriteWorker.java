package io.m0nster.filter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

/**
 * @author PointerRage
 *
 */
public class ReadWriteWorker extends AbstractWorker {
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(16*1024);
	@Override
	protected int work0() {
		int selectedCount;
		try {
			selectedCount = getSelector().selectNow();
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		
		if(selectedCount < 1) {
			return selectedCount;
		}
		
		Set<SelectionKey> keys = getSelector().selectedKeys();
		for(Iterator<SelectionKey> it = keys.iterator(); it.hasNext();) {
			SelectionKey key = it.next();
			it.remove();
			
			if(!key.isValid()) {
				continue;
			}
			
			SelectableChannel channel = key.channel();
			
			try {
				if(key.isReadable() && channel.isOpen()) {
					read(key);
				}
				
				if(key.isWritable() && channel.isOpen()) {
					write(key);
				}
			} catch(CancelledKeyException e) {
				continue;
			}
		}
		
		return selectedCount;
	}
	
	private void read(SelectionKey key) {
		Client client = getClient(key);
		if(client == null) {
			return;
		}
		
		boolean isClient = client.getClientKey() == key;
		if(isClient && client.getServerKey() == null) {
			return;
		}
		
		ReadableByteChannel channel = (ReadableByteChannel) key.channel();
		int readBytes;
		try {
			readBytes = channel.read(buffer);
		} catch(IOException e) {
			client.closeLater();
			return;
		}
		
		if(readBytes < 1) {
			return;
		}
		
		buffer.flip();
		
		ByteBuffer data = ByteBuffer.allocate(readBytes);
		data.put(buffer);
		data.flip();
		if(isClient) {
			client.putServer(data);
			SelectionKey serverKey = client.getServerKey();
			serverKey.interestOps(serverKey.interestOps() | SelectionKey.OP_WRITE);
		} else {
			client.putClient(data);
			SelectionKey clientKey = client.getClientKey();
			clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
		}
		buffer.clear();
	}
	
	private void write(SelectionKey key) {
		Client client = getClient(key);
		if(client == null) {
			return;
		}
		
		boolean isClient = client.getClientKey() == key;
		
		Deque<ByteBuffer> deque = isClient ? client.getClientQueue() : client.getServerQueue();
		do {
			ByteBuffer data = deque.poll();
			if(data == null) {
				break;
			}

			int remaining = buffer.remaining();
			if (remaining >= data.remaining()) {
				buffer.put(data);
			} else {
				int limit = data.limit();
				data.limit(remaining);
				buffer.put(data);
				data.limit(limit);
				data.compact();
				deque.offerFirst(data);
			}
		} while (buffer.hasRemaining());
		
		if(buffer.position() == 0) {
			return;
		}
		buffer.flip();
		
		WritableByteChannel channel = (WritableByteChannel) key.channel();
		int writeBytes;
		try {
			writeBytes = channel.write(buffer);
		} catch(IOException e) {
			client.closeLater();
			return;
		}
		
		if(writeBytes < 1) {
			return;
		}
		
		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		buffer.clear();
	}

}
