package com.speed.irc.types;

public class MessageReader extends Thread {
    public Filter filter;
    public volatile boolean running = true;
    public String s;

    public MessageReader(final Filter f) {
	this.filter = f;
	start();
    }

    @Override
    public synchronized void run() {
	while (running) {
	    try {
		wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    filter.process(s);

	}
    }
}
