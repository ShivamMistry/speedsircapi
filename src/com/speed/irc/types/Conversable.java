package com.speed.irc.types;

/**
 * An abstract representation of a communicable entity on a server.
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
public abstract class Conversable {
	/**
	 * Sends a PRIVMSG message to the entity.
	 * 
	 * @param message
	 *            the message that is to be sent.
	 */
	public abstract void sendMessage(final String message);

	/**
	 * Sends a NOTICE message to the entity.
	 * 
	 * @param notice
	 *            the notice that is to be sent.
	 */
	public abstract void sendNotice(final String notice);

	/**
	 * Gets the name of the entity.
	 * 
	 * @return the name of the channel or user.
	 */
	public abstract String getName();
}
