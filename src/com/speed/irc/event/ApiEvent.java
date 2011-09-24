package com.speed.irc.event;

import com.speed.irc.connection.Server;

public class ApiEvent implements IRCEvent {
	public static final int SERVER_RECONNECTED = 1;

	private int opcode;
	private Server server;
	private Object source;

	public ApiEvent(final int opcode, final Server server, final Object src) {
		source = src;
		this.opcode = opcode;
		this.server = server;
	}

	public int getOpcode() {
		return opcode;
	}

	public Server getServer() {
		return server;
	}

	public Object getSource() {
		return source;
	}

}
