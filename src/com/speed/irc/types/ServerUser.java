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
 * @author Shivam Mistry
 */
public class ServerUser extends Conversable {
	private String nick, host, user;
	private Server server;

	/**
	 * Initialises a server user.
	 * 
	 * @param nick
	 *            the nick of the user
	 * @param host
	 *            the host of the user
	 * @param user
	 *            the username of the user
	 * @param server
	 *            the server the user is on
	 */
	public ServerUser(final String nick, final String host, final String user,
			final Server server) {
		this.nick = nick;
		this.host = host;
		this.user = user;
		this.server = server;
		getServer().addUser(this);
	}

	public String toString() {
		return String.format("%s!%s@%s", nick, user, host);
	}

	/**
	 * Gets the mask of this user.
	 * 
	 * @return the mask of the user
	 */
	public Mask getMask() {
		return new Mask(getNick(),
				getUser() == null || getUser().isEmpty() ? "*" : getUser(),
				getHost() == null || getHost().isEmpty() ? "*" : getHost());
	}

	public void sendMessage(final String message) {
		server.sendRaw(String.format("PRIVMSG %s :%s", nick, message));
	}

	public void sendNotice(final String notice) {
		server.sendNotice(new Notice(notice, null, nick, server));
	}

	public String getName() {
		return nick;
	}

	/**
	 * Gets the nick of this user.
	 * 
	 * @return the nick of the user
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * Gets the host of the user.
	 * 
	 * @return the users host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets the username of this user.
	 * 
	 * @return the username of this user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Gets the server this user is on
	 * 
	 * @return the server this user is on
	 */
	public Server getServer() {
		return server;
	}

	public boolean equals(final Object o) {
		if (!(o instanceof ServerUser))
			return false;
		else {
			ServerUser other = (ServerUser) o;
			return other.getNick().equalsIgnoreCase(nick)
					&& other.getHost().equalsIgnoreCase(getHost())
					&& other.getUser().equalsIgnoreCase(user);
		}
	}
}
