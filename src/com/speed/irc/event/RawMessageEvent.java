package com.speed.irc.event;

import com.speed.irc.types.RawMessage;

/**
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
public class RawMessageEvent implements IRCEvent {

	private RawMessage message;
	protected Object source;

	public RawMessageEvent(final RawMessage message, final Object source) {
		this.message = message;
		this.source = source;
	}

	public RawMessage getMessage() {
		return message;
	}

	public Object getSource() {
		return source;
	}

}
