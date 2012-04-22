package com.speed.irc.event;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages events.
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
public class EventManager implements Runnable {

	private List<IRCEventListener> listeners = new CopyOnWriteArrayList<IRCEventListener>();
	private BlockingQueue<IRCEvent> eventQueue = new LinkedBlockingQueue<IRCEvent>();

	public synchronized void fireEvent(IRCEvent e) {
		eventQueue.add(e);
	}

	public void addListener(final IRCEventListener e) {
		synchronized (listeners) {
			listeners.add(e);
		}
	}

	public void run() {
		IRCEvent e = null;
		e = eventQueue.poll();
		if (e != null) {
			for (IRCEventListener listener : listeners) {
				for (Class<?> clz : listener.getClass().getInterfaces()) {
					if (clz.getAnnotation(ListenerProperties.class) == null) {
						continue;
					} else {
						try {
							ListenerProperties properties = clz
									.getAnnotation(ListenerProperties.class);
							for (Class<? extends IRCEvent> clazz : properties
									.events()) {
								if (e.getClass().isAssignableFrom(clazz)) {
									e.callListener(listener);
								}
							}
						} catch (Exception e1) {
							this.fireEvent(new ExceptionEvent(e1, this, null));
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void clearQueue() {
		eventQueue.clear();

	}

}
