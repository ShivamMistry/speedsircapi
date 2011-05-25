package com.speed.irc.types;

import com.speed.irc.connection.Server;

import java.util.LinkedList;
import java.util.List;

/**
 * A class representing user and channel modes.
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
public class Mode {
    private List<Character> modes = new LinkedList<Character>();
    private final Server server;

    public Mode(final Server server, final String modes) {
        this.server = server;
        if (!modes.isEmpty())
            parse(modes);
    }

    protected void clear() {
        modes.clear();
    }

    public char channelModeLetterToSymbol(char letter) {
        for (int i = 0; i < server.getModeLetters().length; i++) {
            if (server.getModeLetters()[i] == letter) {
                return server.getModeSymbols()[i];
            }
        }
        return '0';
    }

    public char channelModeSymbolToLetter(char symbol) {
        for (int i = 0; i < server.getModeSymbols().length; i++) {
            if (server.getModeSymbols()[i] == symbol) {
                return server.getModeLetters()[i];
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
