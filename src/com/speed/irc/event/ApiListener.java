package com.speed.irc.event;

public interface ApiListener extends IRCEventListener {
	public void apiEventReceived(ApiEvent e);
}
