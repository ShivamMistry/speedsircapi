package com.speed.irc.event;

import com.speed.irc.types.Channel;

/**
 * 
 * Represents a channel event.
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
 * @author Shivam Mistry
 * 
 */
public class ChannelEvent implements IRCEvent {

	public static final int TOPIC_CHANGED = 10, MODE_CHANGED = 11;
	private final int code;
	private final Channel channel;
	private final Object source;

	public ChannelEvent(final Channel channel, final int code,
			final Object source) {
		this.code = code;
		this.channel = channel;
		this.source = source;
	}

	public int getCode() {
		return code;
	}

	public Channel getChannel() {
		return channel;
	}

	public Object getSource() {
		return source;
	}

}
