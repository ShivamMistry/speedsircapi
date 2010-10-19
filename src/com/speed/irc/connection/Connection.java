package com.speed.irc.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import com.speed.irc.types.MessageListener;
import com.speed.irc.types.NOTICE;
import com.speed.irc.types.PRIVMSG;

/**
 * A class representing a socket connection to an IRC server with the
 * functionality of sending raw commands and messages.
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
 * @author Speed
 * 
 */
public class Connection {
	public final BufferedWriter write;
	public final BufferedReader read;
	private final Socket socket;
	private List<MessageListener> listeners = new LinkedList<MessageListener>();
	private final Thread thread;
	private boolean autoJoin;
	private String nick;

	/**
	 * Used to set whether the client should rejoin a channel after being kicked
	 * from it.
	 * 
	 * @param on
	 *            true if the client should rejoin after being kicked.
	 */
	public void setAutoRejoin(final boolean on) {
		autoJoin = on;
	}

	public Connection(Socket sock) throws IOException {
		socket = sock;
		write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		thread = new Thread(new Runnable() {
			public void run() {
				String s;
				try {
					while ((s = read.readLine()) != null) {
						s = s.substring(1);
						if (s.contains("PING") && !s.contains("PRIVMSG")) {
							sendRaw("PONG " + socket.getInetAddress().getHostAddress() + "\n");
						} else if (s.contains("KICK") && (!s.contains("PRIVMSG") || !s.contains("NOTICE"))
								&& s.contains(nick) && autoJoin) {
							Thread.sleep(700);
							joinChannel(s.substring(s.indexOf("KICK")).split(" ")[1]);
						} else if (s.contains("NOTICE")) {

							String message = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
							String sender = s.split("!")[0];
							String channel = null;
							if (s.contains("NOTICE #"))
								channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
							fireNoticeReceived(new NOTICE(message, sender, channel));
						} else if (s.contains(":") && s.substring(0, s.indexOf(":")).contains("PRIVMSG")) {
							String message = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
							String sender = s.split("!")[0];
							String channel = null;
							if (s.contains("PRIVMSG #"))
								channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
							else
								System.out.println("wat");

							fireMessageReceived(new PRIVMSG(message, sender, channel));
						} else {
							fireRawMessageReceived(s);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		thread.start();
	}

	private void fireRawMessageReceived(final String s) {
		for (MessageListener listener : listeners) {
			listener.rawMessageReceived(s);
		}
	}

	private void fireMessageReceived(final PRIVMSG privmsg) {
		for (MessageListener listener : listeners) {
			listener.messageReceived(privmsg);
		}
	}

	private void fireNoticeReceived(final NOTICE notice) {
		for (MessageListener listener : listeners) {
			listener.noticeReceived(notice);
		}
	}

	public void setNick(final String nick) {
		this.nick = nick;
	}

	/**
	 * Adds a listener to handle events.
	 * 
	 * @param listener
	 *            The listener that handles the events.
	 */
	public void addListener(final MessageListener listener) {
		listeners.add(listener);
	}

	/**
	 * Sends a message to a channel/nick
	 * 
	 * @param CHANNEL
	 *            Either the channel or nick you wish to send the message to.
	 * @param message
	 *            The message you wish to send
	 * 
	 */
	public void sendMessage(final PRIVMSG message) {
		sendRaw("PRIVMSG " + message.getChannel() + " :" + message.getMessage() + "\n");
	}

	/**
	 * Sends a raw command to the server, remember to use "\n" for a new line
	 * everytime you send a raw command.
	 * 
	 * @param raw
	 *            The raw command you wish to send to the server.
	 */
	public void sendRaw(String raw) {
		try {
			write.write(raw);
			write.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a notice to the specified nick.
	 * 
	 * @param to
	 *            The nick/channel you wish to send the message to.
	 * @param message
	 *            The message you wish to send.
	 */
	public void sendNotice(final NOTICE notice) {
		sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage() + "\n");
	}

	/**
	 * Joins a channel.
	 * 
	 * @param channel
	 *            The channel you wish to join.
	 */
	public void joinChannel(String channel) {
		sendRaw("JOIN " + channel + "\n");
	}

	/**
	 * Sends an action to a channel/nick.
	 * 
	 * @param channel
	 *            The specified nick you would like to send the action to.
	 * @param action
	 *            The action you would like to send.
	 */
	public void sendAction(String channel, String action) {
		sendRaw("PRIVMSG " + channel + ": ACTION " + action + "");
	}

}
