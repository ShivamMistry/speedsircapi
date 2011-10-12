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
	private boolean isRunning = true;
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
		while (isRunning) {
			IRCEvent e = null;
			e = eventQueue.poll();
			if (e != null)
				try {

					for (IRCEventListener listener : listeners) {
						if (e instanceof NoticeEvent) {
							if (listener instanceof NoticeListener) {
								((NoticeListener) listener)
										.noticeReceived((NoticeEvent) e);
							}
						} else if (e instanceof ApiEvent) {
							if (listener instanceof ApiListener) {
								((ApiListener) listener)
										.apiEventReceived((ApiEvent) e);
							}
						} else if (e instanceof PrivateMessageEvent) {
							if (listener instanceof PrivateMessageListener) {
								((PrivateMessageListener) listener)
										.messageReceived((PrivateMessageEvent) e);
							}
						} else if (e instanceof RawMessageEvent) {
							if (listener instanceof RawMessageListener) {
								((RawMessageListener) listener)
										.rawMessageReceived((RawMessageEvent) e);
							}
						} else if (e instanceof ChannelEvent) {
							if (listener instanceof ChannelUserListener
									&& e instanceof ChannelUserEvent) {
								final ChannelUserListener l = (ChannelUserListener) listener;
								final ChannelUserEvent event = (ChannelUserEvent) e;
								switch (event.getCode()) {
								case ChannelUserEvent.USER_JOINED:
									l.channelUserJoined(event);
									break;
								case ChannelUserEvent.USER_KICKED:
									l.channelUserKicked(event);
									break;
								case ChannelUserEvent.USER_MODE_CHANGED:
									l.channelUserModeChanged(event);
									break;
								case ChannelUserEvent.USER_PARTED:
									l.channelUserParted(event);
									break;
								}
							} else if (listener instanceof ChannelEventListener) {
								final ChannelEventListener l = (ChannelEventListener) listener;
								final ChannelEvent event = (ChannelEvent) e;
								switch (event.getCode()) {
								case ChannelEvent.MODE_CHANGED:
									l.channelModeChanged(event);
									break;
								case ChannelEvent.TOPIC_CHANGED:
									l.channelTopicChanged(event);
									break;
								}
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
		eventQueue.clear();

	}

	public void setRunning(boolean b) {
		this.isRunning = b;
	}

}
