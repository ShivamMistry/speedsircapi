package com.speed.irc.connection;

import com.speed.irc.event.EventManager;
import com.speed.irc.types.Channel;
import com.speed.irc.types.NOTICE;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class representing a socket connection to an IRC server with the
 * functionality of sending raw commands and messages.
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
public class Server implements ConnectionHandler, Runnable {
	private BufferedWriter write;
	private BufferedReader read;
	protected Socket socket;
	protected EventManager eventManager = new EventManager();
	private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	protected Map<String, Channel> channels = new HashMap<String, Channel>();
	private char[] modeSymbols;
	private char[] modeLetters;
	private String serverName;
	private String nick;
	private ServerMessageParser parser;
	protected HashMap<String, String> ctcpReplies = new HashMap<String, String>();
	protected boolean autoConnect;
	private int port;

	public Server(final Socket sock) throws IOException {
		socket = sock;
		port = sock.getPort();
		setServerName(socket.getInetAddress().getHostAddress());
		write = new BufferedWriter(new OutputStreamWriter(
				sock.getOutputStream()));
		read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		Thread eventThread = new Thread(eventManager);
		eventThread.start();
		new Thread(this).start();
		parser = new ServerMessageParser(this);
		ctcpReplies.put("VERSION", "Speed's IRC API");
		ctcpReplies.put("TIME", "Get a watch!");
	}

	public void quit() {
		parser.running = false;

		try {
			getWriter().write("QUIT\n");
			getWriter().flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				getWriter().close();
				getReader().close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void connect() {
		try {
			socket = new Socket(serverName, port);
			write = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			read = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ServerMessageParser getParser() {
		return parser;
	}

	/**
	 * Sets whether the api should auto reconnect if the connection is broken.
	 * Default is <i>off</i>.
	 * 
	 * @param on
	 */
	public void setAutoReconnect(final boolean on) {
		this.autoConnect = on;
	}

	/**
	 * Gets the current nick as captured by the message sending thread.
	 * 
	 * @return the current nick for this server connection.
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * Sets a reply to a CTCP request.
	 * 
	 * @param request
	 *            the request to send the reply for
	 * @param reply
	 *            the reply to send for the request
	 */
	public void setCtcpReply(final String request, final String reply) {
		synchronized (ctcpReplies) {
			ctcpReplies.put(request, reply);
		}
	}

	/**
	 * Sends a raw command to the server.
	 * 
	 * @param raw
	 *            The raw command to be added to the sending queue.
	 */
	public void sendRaw(final String raw) {
		messageQueue.add(raw);
	}

	/**
	 * Gets the channel map.
	 * 
	 * @return the channel map.
	 */
	public Map<String, Channel> getChannels() {
		return channels;
	}

	/**
	 * Gets the buffered writer.
	 * 
	 * @return the buffered writer.
	 */
	public BufferedWriter getWriter() {
		return write;
	}

	/**
	 * Sets the buffered writer.
	 * 
	 * @param write
	 *            the new buffered writer.
	 */
	public void setWrite(final BufferedWriter write) {
		this.write = write;
	}

	/**
	 * Gets the buffered reader.
	 * 
	 * @return the buffered reader.
	 */
	public BufferedReader getReader() {
		return read;
	}

	/**
	 * Sets the buffered reader.
	 * 
	 * @param read
	 *            the new buffered reader.
	 */
	public void setRead(final BufferedReader read) {
		this.read = read;
	}

	/**
	 * Gets the channel access mode symbols (e.g. @ for op)
	 * 
	 * @return the channel access mode symbols.
	 */
	public char[] getModeSymbols() {
		return modeSymbols;
	}

	protected void setModeSymbols(final char[] modeSymbols) {
		this.modeSymbols = modeSymbols;
	}

	/**
	 * Gets the channel access mode letters (e.g. v for voice)
	 * 
	 * @return the channel access mode letters
	 */
	public char[] getModeLetters() {
		return modeLetters;
	}

	protected void setModeLetters(final char[] modeLetters) {
		this.modeLetters = modeLetters;
	}

	/**
	 * Sends a notice to the specified nick.
	 * 
	 * @param notice
	 *            sender can be null.
	 */
	public void sendNotice(final NOTICE notice) {
		sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage()
				+ "\n");
	}

	/**
	 * Sends an action to a channel/nick.
	 * 
	 * @param channel
	 *            The specified channel/nick you would like to send the action
	 *            to.
	 * @param action
	 *            The action you would like to send.
	 */
	public void sendAction(final String channel, final String action) {
		sendRaw("PRIVMSG " + channel + ": \u0001ACTION " + action + "\n");
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void run() {
		while (socket.isConnected()) {

			String s = messageQueue.poll();

			if (s != null) {
				if (!s.endsWith("\n")) {
					s = s + "\n";
				}
				try {
					if (write != null) {
						if (s.startsWith("NICK")) {
							nick = s.replace("NICK", "").replace(":", "")
									.trim();
						}
						write.write(s);
						write.flush();
					} else {
						messageQueue.add(s);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * Gets the server's host address.
	 * 
	 * @return the server's host address.
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Joins a channel on this server if we are not already joined to it.
	 * 
	 * @param channelName
	 *            The name of the channel.
	 * @return The channel object.
	 */
	public Channel joinChannel(final String channelName) {
		if (channels.containsKey(channelName.trim())) {
			final Channel channel = channels.get(channelName);
			if (!channel.isRunning) {
				channel.join();
			}
			return channel;
		}
		final Channel channel = new Channel(channelName, this);
		channel.join();
		return channel;
	}
}
