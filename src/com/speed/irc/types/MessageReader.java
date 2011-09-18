package com.speed.irc.types;

public class MessageReader extends Thread {
	public Filter filter;
	public volatile boolean running = true;
	public String s;
	public Object lock = new Object();

	public MessageReader(final Filter f) {
		this.filter = f;
		start();
	}

	@Override
	public void run() {
		while (running) {
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
				continue;
			}
			filter.process(s);

		}
	}
}
