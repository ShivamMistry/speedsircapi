package com.speed.irc.types;

import com.speed.irc.connection.Server;

/**
 * A representation of a user an a server.
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
 * @author Speed
 */
public class ServerUser extends Conversable {
	private String nick, host, user;
	private Server server;

	public ServerUser(final String nick, final String host, final String user,
			final Server server) {
		this.nick = nick;
		this.host = host;
		this.user = user;
		this.server = server;
	}

	public String toString() {
		return String.format("%s!%s@%s", nick, user, host);
	}

	public void sendMessage(final String message) {
		server.sendRaw(String.format("PRIVMSG %s :%s", nick, message));
	}

	public void sendNotice(final String notice) {
		server.sendRaw(String.format("NOTICE %s :%s", nick, notice));
	}

	public String getName() {
		return nick;
	}

	public String getNick() {
		return nick;
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public Server getServer() {
		return server;
	}
}
