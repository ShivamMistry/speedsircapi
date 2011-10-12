package com.speed.irc.event;

import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

/**
 * 
 * Represents a channel user event.
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
public class ChannelUserEvent extends ChannelEvent {
	private Object source;
	private Channel channel;
	private ChannelUser user;
	private int code;
	public static final int USER_JOINED = 0, USER_PARTED = 1,
			USER_MODE_CHANGED = 2, USER_KICKED = 3;

	public ChannelUserEvent(Object source, final Channel channel,
			final ChannelUser user, final int code) {
		super(channel, code, source);
		this.source = source;
		this.channel = channel;
		this.user = user;
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public Object getSource() {
		return source;
	}

	public Channel getChannel() {
		return channel;
	}

	public ChannelUser getUser() {
		return user;
	}

}
