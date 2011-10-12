package com.speed.irc.types;

/**
 * A wrapper class for PRIVMSGs.
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
public class PRIVMSG {

	private final String message, sender;
	private Conversable conversable;

	/**
	 * @param message
	 *            The actual message.
	 * @param sender
	 *            The nick of the person who the message was sent to/from.
	 * @param conversable
	 *            The channel the message was sent to/from.
	 */
	public PRIVMSG(final String message, final String sender,
			final Conversable conversable) {
		this.message = message;
		this.conversable = conversable;
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public String getSender() {
		return sender;
	}

	public Conversable getConversable() {
		return conversable;
	}

	public boolean isPrivateMessage() {
		return !conversable.getName().contains("#");
	}

	public boolean isChannelMessage() {
		return conversable.getName().contains("#");
	}

	public boolean isCtcpMessage() {
		return message.startsWith("\u0001");
	}

}
