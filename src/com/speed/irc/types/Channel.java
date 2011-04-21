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
	private String name;
	private Connection connection;
	public static final int WHO_RESPONSE = 352, WHO_END = 315;
	private List<ChannelUser> users = new LinkedList<ChannelUser>();
	private List<ChannelUser> tempList = new LinkedList<ChannelUser>();
	public volatile boolean isRunning = true;
	public static final int WHO_DELAY = 90000;
	private boolean autoRejoin;
	private String nick;
	private Mode chanMode;
	private List<String> bans = new LinkedList<String>();

	public Channel(String name, Connection connection, String nick) {
		this.name = name;
		this.connection = connection;
		this.nick = nick;
		this.connection.channels.put(name, this);

		this.connection.getEventManager().addListener(this);
		new Thread(this).start();
	}

	public String getName() {
		return name;
	}

	public List<ChannelUser> getUsers() {
		return users;
	}

	public void setAutoRejoin(boolean on) {
		autoRejoin = on;
	}

	public void sendMessage(String message) {
		connection.sendMessage(new PRIVMSG(message, null, this));
	}

	public void rawMessageReceived(RawMessageEvent e) {
		String raw = e.getMessage().getRaw();
		String code = e.getMessage().getCode();
		if (autoRejoin && code.equals("KICK") && raw.split(" ")[3].equals(nick)) {
			join();
		}
		if (code.equals("KICK")) {
			String nick = raw.split(" ")[3];
			for (ChannelUser user : users) {
				if (user.getNick().equals(nick)) {
					users.remove(user);
				}
			}
		}
		if (code.equals("JOIN")) {
			String nick = raw.split("!")[0];
			String user = raw.substring(raw.indexOf("!") + 1, raw.indexOf("@"));
			String host = raw.substring(raw.indexOf("@") + 1, raw.indexOf("J")).trim();
			users.add(new ChannelUser(nick, "", user, host));
		}
		if (code.equals("MODE")) {
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
					int plus = modes.indexOf("+");
					int minus = modes.indexOf("-");
					char[] pluses;
					char[] minuses = null;
					int index = -1;
					if (plus != -1) {
						String s = null;
						if (minus != -1 && minus > plus) {
							s = modes.substring(plus + 1, minus);
						} else if (minus != -1 && minus < plus) {
							s = modes.substring(plus + 1);
						} else {
							s = modes.substring(1);
						}
						if (s != null) {

							pluses = new char[s.length()];
							for (int i = 0; i < pluses.length; i++) {
								pluses[i] = s.toCharArray()[i];
								++index;
								if (pluses[i] == 'b') {
									bans.add(u[index]);
									continue;
								}
								for (ChannelUser user : users) {
									if (user.getNick().equals(u[index])) {
										user.addMode(pluses[i]);
									}
								}
							}
						}
					}
					if (minus != -1) {
						String s = null;
						if (plus != -1 && plus > minus) {
							s = modes.substring(minus + 1, plus);
						} else if (minus != -1 && plus < minus) {
							s = modes.substring(minus + 1);
						} else {
							s = modes.substring(1);
						}
						if (s != null)
							minuses = new char[s.length()];
						for (int i = 0; i < minuses.length; i++) {
							minuses[i] = s.toCharArray()[i];
							++index;
							if (minuses[i] == 'b') {
								bans.remove(u[index]);
								continue;
							}
							for (ChannelUser user : users) {
								if (user.getNick().equals(u[index])) {
									user.removeMode(minuses[i]);
								}
							}
						}

					}
				}
			}
		}
		boolean isNumber = true;

		for (char c : code.toCharArray()) {
			if (!Character.isDigit(c)) {
				isNumber = false;
				break;
			}
		}
		if (isNumber) {
			int i = Integer.parseInt(code);
			if (i == Channel.WHO_RESPONSE) {
				if (raw.contains(name)) {
					String[] temp = raw.split(" ");
					String user = temp[4];
					String host = temp[5];
					String nick = temp[7];
					String modes = temp[8];
					modes = modes.replace("*", "").replace("G", "").replace("H", "");
					tempList.add(new ChannelUser(nick, modes, user, host));
				}
			} else if (i == Channel.WHO_END) {
				users.clear();
				users.addAll(tempList);
				tempList.clear();
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

	public void join() {
		connection.joinChannel(name);
	}

}