package com.speed.irc.types;

/**
 * Represents a user in a channel.
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
public class ChannelUser {
	private String nick, modes, user;
	private String host;
	private Mode channelModes = new Mode("");

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getModes() {
		return modes;
	}

	public void setModes(String modes) {
		this.modes = modes;
	}

	public ChannelUser(String nick, String modes, String user, String host) {
		this.setModes(modes);
		this.setNick(nick);
		this.setHost(host);
		this.setUser(user);
		if (!modes.isEmpty())
			sync(modes);
	}

	public void sync(String modes) {
		channelModes.clear();
		String s = "+";
		if (modes.contains("@")) {
			s += "o";
		} else if (modes.contains("+")) {
			s += "v";
		} else if (modes.contains("&")) {
			s += "a";
		} else if (modes.contains("~")) {
			s += "q";
		} else if (modes.contains("%")) {
			s += "h";
		}
		channelModes.parse(modes);
	}

	public void addMode(char mode) {
		mode = chanModeToSymbol(mode);

		modes = modes + mode;
		sync(modes);
	}

	public char chanModeToSymbol(char mode) {
		if (mode == 'v')
			return '+';
		if (mode == 'o')
			return '@';
		if (mode == 'a')
			return '&';
		if (mode == 'q')
			return '~';
		if (mode == 'h')
			return '%';
		return '0';
	}

	public void removeMode(char mode) {
		mode = chanModeToSymbol(mode);
		StringBuilder builder = new StringBuilder();
		for (char c : modes.toCharArray()) {
			if (c != mode) {
				builder.append(c);
			}
		}
		modes = builder.toString();
		sync(modes);
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public String getHost() {
		return host;
	}

	public boolean isOperator() {
		return modes.contains("@");
	}

	public boolean isHalfOperator() {
		return modes.contains("%");
	}

	public boolean isVoiced() {
		return modes.contains("+");
	}

	public boolean isOwner() {
		return modes.contains("~");
	}

	public boolean isProtected() {
		return modes.contains("&");
	}

	public int getRights() {
		if (isOwner()) {
			return 5;
		} else if (isProtected()) {
			return 4;
		} else if (isOperator()) {
			return 3;

		} else if (isHalfOperator()) {
			return 2;
		} else if (isVoiced()) {
			return 1;
		}
		return 0;
	}
}
