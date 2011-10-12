package com.speed.irc.event;

import com.speed.irc.types.PRIVMSG;

/**
 * The wrapper class for an PRIVMSG event.
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
 * @author Shivam Mistry
 * 
 */
public class PrivateMessageEvent implements IRCEvent {

	protected Object source;
	protected PRIVMSG message;

	public PrivateMessageEvent(final PRIVMSG message, final Object source) {
		this.source = source;
		this.message = message;
	}

	public Object getSource() {
		return source;
	}

	public PRIVMSG getMessage() {
		return message;
	}

}
