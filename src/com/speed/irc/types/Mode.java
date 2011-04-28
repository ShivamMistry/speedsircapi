package com.speed.irc.types;

import java.util.LinkedList;
import java.util.List;

import com.speed.irc.connection.Connection;

/**
 * A class representing user and channel modes.
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
public class Mode {
	private List<Character> modes = new LinkedList<Character>();

	public Mode(String modes) {
		if (!modes.isEmpty())
			parse(modes);
	}

	protected void clear() {
		modes.clear();
	}

	public static char channelModeLetterToSymbol(char letter) {
		for (int i = 0; i < Connection.modeLetters.length; i++) {
			if (Connection.modeLetters[i] == letter) {
				return Connection.modeSymbols[i];
			}
		}
		return '0';
	}

	public static char channelModeSymbolToLetter(char symbol) {
		for (int i = 0; i < Connection.modeSymbols.length; i++) {
			if (Connection.modeSymbols[i] == symbol) {
				return Connection.modeLetters[i];
			}
		}
		return '0';
	}

	protected void parse(String modes) {
		boolean plus = false;
		int index = 0;
		for (int i = 0; i < modes.toCharArray().length; i++) {
			char c = modes.toCharArray()[i];
			if (c == '+') {
				plus = true;
				continue;
			} else if (c == '-') {
				plus = false;
				continue;
			}
			index++;
			if (plus) {
				this.modes.add(c);
			} else {
				this.modes.remove(c);
			}
		}
	}
}
