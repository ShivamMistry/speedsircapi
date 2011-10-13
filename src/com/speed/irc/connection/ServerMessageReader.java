package com.speed.irc.connection;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.speed.irc.event.ApiEvent;

/**
 * Reads messages from the server and adds them to a queue. Encapsulates the
 * queue to prevent it being read and modified before the parser parses the
 * messages.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Shivam Mistry
 */
public class ServerMessageReader implements Runnable {
	private final Server server;
	private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	private volatile String current;

	/**
	 * No public access to queue to prevent reading before the parser. Gets the
	 * next message to be read.
	 * 
	 * @return the next message
	 */
	protected String poll() {
		return queue.poll();
	}

	/**
	 * Gets the next item on the queue without removing it from the queue.
	 * 
	 * @return the next item on queue
	 */
	public String peek() {
		return queue.peek();
	}

	/**
	 * Checks to see if the queue is empty.
	 * 
	 * @return true if they queue is empty, else false.
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * No public access to queue to prevent reading before the parser. Gets the
	 * queue
	 * 
	 * @return the queue
	 */
	protected Queue<String> getQueue() {
		return queue;
	}

	public ServerMessageReader(final Server server) {
		this.server = server;
	}

	public void run() {
		try {
			while (server.isConnected()
					&& (current = server.getReader().readLine()) != null) {
				try {
					queue.add(current);
				} catch (IllegalStateException e) {// should only happen if the
													// parser dies
					queue.clear();
					queue.add(current);
				}
			}
		} catch (IOException e) {
			try {
				server.getWriter().close();
				server.getReader().close();
				server.socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (server.autoConnect) {
				server.connect();
				server.eventManager.fireEvent(new ApiEvent(
						ApiEvent.SERVER_RECONNECTED, server, this));
			}
		}
	}

}
