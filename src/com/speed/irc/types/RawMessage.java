package com.speed.irc.types;

/**
 * Represents a raw message.
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
public class RawMessage {

	private String raw;
	private String code;
	private String sender;
	private String target;

	public RawMessage(String raw) {
		this.raw = raw;
		String[] strings = raw.split(" ");
		code = strings[1];
		sender = strings[0];
		if (strings.length > 2)
			target = strings[2];

	}

	public String getRaw() {
		return raw;
	}

	public String getCommand() {
		return code;
	}

	public String getTarget() {
		return target;
	}

	public String getSender() {
		return sender;
	}
}
