package io.m0nster.filter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author PointerRage
 *
 */
public class ConnectWorker extends AbstractWorker {

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
			
			if(key.isConnectable()) {
				SocketChannel channel = (SocketChannel) key.channel();
				Client client = getClient(key);
				try {
					if(channel.finishConnect()) {
						SelectionKey serverKey = WorkerManager.getInstance().getReadWriteWorker().register(channel, SelectionKey.OP_READ, client);
						client.setServerKey(serverKey);
						key.cancel();
					}
				} catch (IOException e) {
					client.closeLater();
				}
			}
		}
		
		return selectedCount;
	}

}
