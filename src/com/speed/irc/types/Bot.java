package com.speed.irc.types;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.speed.irc.connection.Connection;

/**
 * The abstract class for making bots. To create a bot, you can extend this
 * class.
 * 
 * This file is part of Speed's IRC API.
 * 
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * @author Speed
 * 
 */
public abstract class Bot implements Runnable {

	public Connection connection;
	private final String server;
	private final int port;
	private boolean autoJoin;

	public int getPort() {
		return port;
	}

	public String getServer() {
		return server;
	}

	/**
	 * 
	 * @param on
	 *            true if the client should rejoin after being kicked.
	 */
	public void setAutoRejoin(final boolean on) {
		autoJoin = on;
	}

	public void run() {
		try {
			connection.write.write("NICK " + getNick() + "\n");
			connection.write.write("USER " + getUser() + " team-deathmatch.com TB: Speed Bot\n");

			for (String s : getChannels()) {
				connection.write.write("JOIN " + s + "\n");
			}
			connection.write.flush();
			onStart();
			String wat;
			while ((wat = connection.read.readLine()) != null) {
				System.out.println(wat);
				wat = wat.substring(1);
				if (wat.contains("PING") && !wat.contains("PRIVMSG")) {
					connection.sendRaw("PONG " + server + "\n");
				} else if (wat.contains("NOTICE")) {
					String message = wat.substring(wat.indexOf(" :")).trim().replaceFirst(":", "");
					String sender = wat.split("!")[0];
					String channel;
					if (wat.contains("PRIVMSG #"))
						channel = wat.substring(wat.indexOf("#")).split(" ")[0];
					else
						channel = null;
					onNotice(new NOTICE(message, sender, channel));
				} else if (wat.contains(":")) {
					String raw = wat.substring(0, wat.indexOf(":"));
					if (raw.contains("KICK") && raw.contains(getNick()) && autoJoin) {
						Thread.sleep(1000);
						connection.joinChannel(raw.split(" ")[2]);
					}
				} else if (wat.contains(":") && wat.substring(0, wat.indexOf(":")).contains("PRIVMSG")) {
					String message = wat.substring(wat.indexOf(" :")).trim().replaceFirst(":", "");
					String sender = wat.split("!")[0];
					String channel;
					if (wat.contains("PRIVMSG #"))
						channel = wat.substring(wat.indexOf("#")).split(" ")[0];
					else
						channel = null;

					onMessage(new PRIVMSG(message, sender, channel));
				} else {
					onRawCommand(wat);
				}

			}
			connection.read.close();
			connection.write.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract void onStart();

	public Bot(final String server, final int port) {
		this.server = server;
		this.port = port;
		try {
			connection = new Connection(new Socket(server, port));
			new Thread(this).start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public abstract String[] getChannels();

	public abstract String getNick();

	public String getUser() {
		return "Speed";
	}

	public void identify(String password) throws IOException {
		connection.write.write("PRIVMSG NickServ :identify " + password + "\n");
	}

	public abstract void onRawCommand(String raw);

	public abstract void onMessage(final PRIVMSG message);

	public abstract void onNotice(final NOTICE notice);
}
