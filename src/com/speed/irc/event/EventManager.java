package com.speed.irc.event;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages events.
 * 
 * This file is part of Speed's IRC API.
 * 
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Speed
 * 
 */
public class EventManager implements Runnable {

	private List<IRCEventListener> listeners = new LinkedList<IRCEventListener>();
	private boolean isRunning = true;
	private BlockingQueue<IRCEvent> eventQueue = new LinkedBlockingQueue<IRCEvent>();

	public void fireEvent(IRCEvent e) {
		synchronized (eventQueue) {
			eventQueue.add(e);
		}
	}

	public void addListener(final IRCEventListener e) {
		synchronized (listeners) {
			listeners.add(e);
		}
	}

	public void run() {
		while (isRunning) {
			IRCEvent e = null;
			synchronized (eventQueue) {
				e = eventQueue.poll();
			}
			if (e != null)
				try {
					for (IRCEventListener listener : listeners) {
						if (e instanceof NoticeEvent) {
							if (listener instanceof NoticeListener) {
								((NoticeListener) listener).noticeReceived((NoticeEvent) e);
							}
						} else if (e instanceof PrivateMessageEvent) {
							if (listener instanceof PrivateMessageListener) {
								((PrivateMessageListener) listener).messageReceived((PrivateMessageEvent) e);
							}
						} else if (e instanceof RawMessageEvent) {
							if (listener instanceof RawMessageListener) {
								((RawMessageListener) listener).rawMessageReceived((RawMessageEvent) e);
							}
						}
					}
				} catch (Exception ea) {
					ea.printStackTrace();
					continue;
				}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void clearQueue() {
		synchronized (eventQueue) {
			eventQueue.clear();
		}
	}

	public void setRunning(boolean b) {
		this.isRunning = b;
	}

}
