package com.speed.irc.types;

import com.speed.irc.connection.Server;
import com.speed.irc.event.IRCEventListener;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The abstract class for making robots. To create a robot, you can extend this
 * class.
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
public abstract class Bot {

	public Server server;
	private final int port;

	public int getPort() {
		return port;
	}

	public abstract void onStart();

	public Bot(final String server, final int port) {
		this.port = port;

		try {
			this.server = new Server(new Socket(server, port));
			this.server.sendRaw("NICK " + getNick() + "\n");
			this.server.sendRaw("USER " + getNick()
					+ " team-deathmatch.com TB: Speed Bot\n");
			if (this instanceof IRCEventListener) {
				this.server.getEventManager().addListener(
						(IRCEventListener) this);
			}
			for (Channel s : getChannels()) {
				s.join();
			}
			onStart();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public abstract Channel[] getChannels();

	public abstract String getNick();

	public String getUser() {
		return "Speed";
	}

	/**
	 * Used to identify to NickServ.
	 * 
	 * @param password
	 *            The password assigned to your nick
	 */
	public void identify(final String password) {
		server.sendRaw("PRIVMSG NickServ :identify " + password + "\n");
	}
}
