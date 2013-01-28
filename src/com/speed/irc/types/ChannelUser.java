package com.speed.irc.types;

/**
 * Represents a user in a channel.
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
public class ChannelUser extends ServerUser {
	private String nick, modes, user;
	private String host;
	private Mode channelModes;
	private final Channel channel;
	public static final int VOICE_FLAG = 0x1, HALF_OP_FLAG = 0x2,
			OP_FLAG = 0x4, ADMIN_FLAG = 0x8, OWNER_FLAG = 0x10;

	public String getNick() {// docs from superclass are sufficient
		return nick;
	}

	protected void setNick(String nick) {
		this.nick = nick;
	}

	/*
	 * public String getModes() { return modes; } //dont need this any more
	 * public void setModes(String modes) { this.modes = modes; }
	 */

	public Channel getChannel() {
		return channel;
	}

	public ChannelUser(final String nick, final String modes,
			final String user, final String host, final Channel channel) {
		super(nick, host, user, channel.getServer());
		this.channel = channel;
		this.channelModes = new Mode(this.channel.server, "");
		this.modes = (modes);
		this.setNick(nick);
		this.setHost(host);
		this.setUser(user);
		if (!modes.isEmpty())
			sync(modes);
	}

	public void sync(String modes) {
		channelModes.clear();
		StringBuilder builder = new StringBuilder("+");
		for (char c : modes.toCharArray()) {
			builder.append(channelModes.channelModeSymbolToLetter(c));
		}
		channelModes.parse(builder.toString());
	}

	public void addMode(char mode) {
		mode = channelModes.channelModeLetterToSymbol(mode);

		modes = modes + mode;
		sync(modes);
	}

	public void removeExempts() {
		for (final String s : channel.exempts) {
			Mask mask = new Mask(s);
			if (mask.matches(this)) {
				channel.removeExempt(s);
			}
		}
	}

	public void removeMode(char mode) {
		mode = channelModes.channelModeLetterToSymbol(mode);
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
		return (getRights() & OP_FLAG) != 0;
	}

	public boolean isHalfOperator() {
		return (getRights() & HALF_OP_FLAG) != 0;
	}

	public boolean isVoiced() {
		return (getRights() & VOICE_FLAG) != 0;
	}

	public boolean isOwner() {
		return (getRights() & OWNER_FLAG) != 0;
	}

	public boolean isProtected() {
		return (getRights() & ADMIN_FLAG) != 0;
	}

	/**
	 * Useful if you're only checking for a single flag.
	 * 
	 * @returns the bitmask of the user's flags
	 */
	public int getRights() {
		int rights = 0;
		if (modes.indexOf(channel.server.getModeSymbols()[0]) != -1)
			rights = rights | OWNER_FLAG;
		if (modes.indexOf(channel.server.getModeSymbols()[1]) != -1)
			rights = rights | ADMIN_FLAG;
		if (modes.indexOf(channel.server.getModeSymbols()[2]) != -1)
			rights = rights | OP_FLAG;
		if (modes.indexOf(channel.server.getModeSymbols()[3]) != -1)
			rights = rights | HALF_OP_FLAG;
		if (modes.indexOf(channel.server.getModeSymbols()[4]) != -1)
			rights = rights | VOICE_FLAG;
		return rights;
	}

	@Override
	public String toString() {
		return nick;
	}
}
