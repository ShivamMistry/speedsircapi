package com.speed.irc.types;

import java.util.LinkedList;
import java.util.List;

import com.speed.irc.connection.Connection;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.event.RawMessageListener;

/**
 * Represents a channel
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
public class Channel implements RawMessageListener, Runnable {
	protected String name;
	protected Connection connection;
	protected List<ChannelUser> users = new LinkedList<ChannelUser>();
	protected List<ChannelUser> tempList = new LinkedList<ChannelUser>();
	public volatile boolean isRunning = true;
	public static final int WHO_DELAY = 90000;
	protected boolean autoRejoin;
	protected String nick;
	protected Mode chanMode;
	protected List<String> bans = new LinkedList<String>();
	protected String topic;
	protected Thread channel;

	/**
	 * Constructs a channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @param connection
	 *            the connection which to register the channel to.
	 * @param nick
	 *            the nick of the client.
	 */
	public Channel(final String name, final Connection connection, final String nick) {
		this.name = name;
		this.connection = connection;
		this.nick = nick;
		this.connection.channels.put(name, this);
		this.connection.getEventManager().addListener(this);
	}

	/**
	 * Gets the name of the channel.
	 * 
	 * @return the name of the channel
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the list of users in the channel.
	 * 
	 * @return The list of users in the channel.
	 */
	public List<ChannelUser> getUsers() {
		return users;
	}

	/**
	 * Gets a user from the channel.
	 * 
	 * @param nick
	 *            The nick of the ChannelUser to get.
	 * @return The ChannelUser object associated with the nick or
	 *         <code>null</code>.
	 */
	public ChannelUser getUser(final String nick) {
		for (ChannelUser user : users) {
			if (user.getNick().equals(nick)) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Sets whether rejoining is enabled when kicked
	 * 
	 * @param on
	 *            turn auto-rejoin on or not
	 */
	public void setAutoRejoin(final boolean on) {
		autoRejoin = on;
	}

	/**
	 * Leaves the channel.
	 * 
	 * @param message
	 *            The part message, can be null for no message.
	 */
	public void part(final String message) {
		isRunning = false;
		if (message == null)
			connection.sendRaw(String.format("PART %s :%s\n", name, message));
		else
			connection.sendRaw(String.format("PART %s\n", name, message));
	}

	/**
	 * Sends a message to the channel.
	 * 
	 * @param message
	 *            The message to be sent
	 */
	public void sendMessage(final String message) {
		connection.sendRaw(String.format("PRIVMSG %s :%s\n", name, message));
	}

	public void rawMessageReceived(final RawMessageEvent e) {
		String raw = e.getMessage().getRaw();
		String code = e.getMessage().getCommand();
		if (code.equals("KICK") && raw.split(" ")[3].equals(nick)) {
			if (autoRejoin)
				join();
			else
				isRunning = false;
		} else if (code.equals("KICK")) {
			String nick = raw.split(" ")[3];
			for (ChannelUser user : users) {
				if (user.getNick().equals(nick)) {
					users.remove(user);
				}
			}
		} else if (code.equals("JOIN")) {
			String nick = raw.split("!")[0];
			String user = raw.substring(raw.indexOf("!") + 1, raw.indexOf("@"));
			String host = raw.substring(raw.indexOf("@") + 1, raw.indexOf("J")).trim();
			users.add(new ChannelUser(nick, "", user, host, this));
		} else if (code.equals("MODE")) {
			raw = raw.substring(raw.indexOf(code));
			if (raw.contains(name)) {
				raw = raw.substring(raw.indexOf(name) + name.length()).trim();
				String[] strings = raw.split(" ");
				String modes = strings[0];
				if (strings.length == 1) {
					chanMode.parse(modes);
				} else {
					String[] u = new String[strings.length - 1];
					System.arraycopy(strings, 1, u, 0, u.length);
					boolean plus = false;
					int index = 0;
					for (int i = 0; i < modes.toCharArray().length; i++) {
						char c = modes.toCharArray()[i];
						if (c == '+') {
							plus = true;
							continue;
						} else if (c == '-') {
							plus = false;
							continue;
						}
						index++;
						if (c == 'b') {
							if (plus) {
								bans.add(u[index]);
							} else {
								bans.remove(u[index]);
							}
							continue;
						}
						for (ChannelUser user : users) {
							if (user.getNick().equals(u[index])) {
								if (plus) {
									user.addMode(c);
								} else {
									user.removeMode(c);
								}
							}
						}

					}

				}
			}
		} else if (code.equals(Numerics.WHO_RESPONSE)) {
			if (raw.contains(name)) {
				String[] temp = raw.split(" ");
				String user = temp[4];
				String host = temp[5];
				String nick = temp[7];
				String modes = temp[8];
				modes = modes.replace("*", "").replace("G", "").replace("H", "");
				tempList.add(new ChannelUser(nick, modes, user, host, this));
			}
		} else if (code.equals(Numerics.WHO_END)) {
			users.clear();
			users.addAll(tempList);
			tempList.clear();
		} else if (code.toLowerCase().equals("topic")) {
			String[] temp = raw.split(" :", 2);
			if (temp[0].substring(temp[0].indexOf("TOPIC")).contains(name)) {
				setTopic(temp[1]);
			}
		} else if (code.equals(Numerics.BANNED_FROM_CHANNEL) && e.getMessage().getTarget().equals(nick)) {
			isRunning = false;
		} else if (code.equals("NICK")) {
			ChannelUser user = getUser(e.getMessage().getSender().split("!")[0]);
			if (user != null) {
				user.setNick(raw.substring(raw.indexOf(": ") + 2).trim());
			}
		}

	}

	public void run() {
		do {
			connection.sendRaw("WHO " + name);
			if (!users.isEmpty())
				try {
					Thread.sleep(Channel.WHO_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			else {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while (isRunning);
	}

	/**
	 * Joins the channel.
	 */
	public void join() {
		connection.joinChannel(name);

		isRunning = true;
		if (!channel.isAlive()) {
			channel = new Thread(this);
			channel.start();
		}
	}

	/**
	 * Doesn't send any topic changes to the server.
	 * 
	 * @param topic
	 */
	public void setTopic(final String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

}