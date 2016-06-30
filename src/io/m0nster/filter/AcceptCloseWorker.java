package io.m0nster.filter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author PointerRage
 *
 */
public class AcceptCloseWorker extends AbstractWorker {
	
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
			
			if(key.isAcceptable()) {
				ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
				SocketChannel channel;
				try {
					while((channel = serverChannel.accept()) != null) {
						channel.configureBlocking(false);
						
						SelectionKey rwKey = WorkerManager.getInstance().getReadWriteWorker().register(channel, SelectionKey.OP_READ, null);
						Client client = new Client(rwKey);
						rwKey.attach(client);
						
						selectedCount++;
					}
				} catch(IOException e) {
					throw new UncheckedIOException(e);
				}
				
				continue;
			}
			
			if(key.isConnectable()) { //use connectable as closeable
				Client client = getClient(key);
				if(!client.getClientQueue().isEmpty() || !client.getServerQueue().isEmpty()) {
					continue;
				}
				
				client.close();
				try {
					key.channel().close();
				} catch(IOException e) {
					
				}
				key.cancel();
			}
		}
		
		return selectedCount;
	}
	
}
