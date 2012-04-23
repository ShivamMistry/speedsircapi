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

	/**
	 * @deprecated see {@link #dispatchEvent(IRCEvent)} instead
	 * @param e
	 */
	public synchronized void fireEvent(IRCEvent e) {
		dispatchEvent(e);
	}

	/**
	 * Adds an event to the event queue.
	 * 
	 * @param event
	 *            the event to be processed by the event queue.
	 */
	public synchronized void dispatchEvent(final IRCEvent event) {
		eventQueue.add(event);
	}

	/**
	 * Adds an event listener to this event manager.
	 * 
	 * @param listener
	 *            the listener to be added to this event manager
	 */
	public synchronized void addListener(final IRCEventListener listener) {
		listeners.add(listener);
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
							this.dispatchEvent(new ExceptionEvent(e1, this, null));
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Clears the queue of events to be processed.
	 */
	public void clearQueue() {
		eventQueue.clear();

	}

}
