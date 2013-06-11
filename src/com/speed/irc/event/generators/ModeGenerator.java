package com.speed.irc.event.generators;

import com.speed.irc.connection.Server;
import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.channel.ChannelEvent;
import com.speed.irc.event.channel.ChannelUserEvent;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.RawMessage;

/**
 * Processes MODE messages sent from the server.
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
public class ModeGenerator implements EventGenerator {

    public boolean accept(RawMessage raw) {
        return raw.getCommand().equals("MODE");
    }

    public IRCEvent generate(RawMessage message) {
        String raw = message.getRaw();
        Server server = message.getServer();
        String sender = message.getSender();
        String name = message.getTarget();
        Channel channel = null;
        if (name.startsWith("#")) {
            channel = server.getChannel(name);
            if (!channel.isRunning()) {
                channel.setup();
            }
        }
        if (name.equals(server.getNick())) {
            String modes = raw.split(" :", 2)[1].trim();
            server.parseUserModes(modes);
            return null;
        }
        String senderNick = sender.split("!")[0].trim();
        raw = raw.split(name, 2)[1].trim();
        String[] strings = raw.split(" ");
        String modes = strings[0];
        if (strings.length == 1 && channel != null) {
            channel.chanMode.parse(modes);
            return new ChannelEvent(channel, ChannelEvent.MODE_CHANGED, senderNick, this, modes);
        } else {
            String[] u = new String[strings.length - 1];
            System.arraycopy(strings, 1, u, 0, u.length);
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
                if (c == 'b') {
                    if (plus) {
                        channel.bans.add(u[index]);
                    } else {
                        channel.bans.remove(u[index]);
                    }
                    server.getEventManager().dispatchEvent(
                            new ChannelEvent(channel, ChannelEvent.MODE_CHANGED, senderNick, this, (plus ? "+" : "-")
                                    + "b", u[index]));
                    index++;

                    continue;
                } else if (c == 'e') {
                    if (plus) {
                        channel.exempts.add(u[index]);
                    } else {
                        channel.exempts.remove(u[index]);
                    }
                    server.getEventManager().dispatchEvent(
                            new ChannelEvent(channel, ChannelEvent.MODE_CHANGED, senderNick, this, (plus ? "+" : "-")
                                    + "e", u[index]));
                    index++;

                    continue;
                } else if (c == 'I') {
                    if (plus) {
                        channel.invites.add(u[index]);
                    } else {
                        channel.invites.remove(u[index]);
                    }
                    server.getEventManager().dispatchEvent(
                            new ChannelEvent(channel, ChannelEvent.MODE_CHANGED, senderNick, this, (plus ? "+" : "-")
                                    + "I", u[index]));
                    index++;

                    continue;
                }
                ChannelUser user = channel.getUser(u[index]);
                if (user != null) {
                    if (plus) {
                        user.addMode(c);
                    } else {
                        user.removeMode(c);
                    }
                    server.getEventManager().dispatchEvent(
                            new ChannelUserEvent(this, channel, user, senderNick, ChannelUserEvent.USER_MODE_CHANGED,
                                    (plus ? "+" : "-") + c));

                }
                index++;

            }

        }
        return null;
    }

}
