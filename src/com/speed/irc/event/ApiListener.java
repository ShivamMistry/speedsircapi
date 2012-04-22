package com.speed.irc.event;
@ListenerProperties(events = ApiEvent.class)
public interface ApiListener extends IRCEventListener {
	public void apiEventReceived(ApiEvent e);
}
