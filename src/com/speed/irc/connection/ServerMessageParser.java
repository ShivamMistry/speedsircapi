package com.speed.irc.connection;

import com.speed.irc.event.ChannelUserEvent;
import com.speed.irc.event.NoticeEvent;
import com.speed.irc.event.PrivateMessageEvent;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.types.*;

/**
 * Processes messages sent from the server.
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
public class ServerMessageParser implements Runnable {
    private final Server server;

    public ServerMessageParser(final Server server) {
        this.server = server;
    }

    public void run() {
        String s;
        try {
            while ((s = server.getReader().readLine()) != null) {
                s = s.substring(1);
                final RawMessage message = new RawMessage(s);
                String raw = message.getRaw();
                String code = message.getCommand();
                if (s.startsWith("PING")) {
                    server.sendRaw("PONG" + s.replaceFirst("PING", "") + "\n");
                } else if (message.getCommand().equals("PRIVMSG")) {
                    final String msg = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
                    final String sender = message.getSender().split("!")[0];
                    final String user = message.getSender().substring(message.getSender().indexOf("!") + 1, message.getSender().indexOf("@"));
                    final String host = message.getSender().substring(message.getSender().indexOf("@") + 1);

                    final String name = message.getRaw().split(" ")[2];
                    if (msg.startsWith("\u0001")) {
                        String request = msg.replace("\u0001", "");
                        synchronized (server.ctcpReplies) {
                            if (server.ctcpReplies.containsKey(request)) {
                                server.sendRaw(String.format("NOTICE %s :\u0001%s %s\u0001\n", sender, request, server.ctcpReplies.get(request)));
                            }
                        }
                    }
                    Conversable conversable = null;
                    if (s.contains("PRIVMSG #")) {
                        conversable = server.channels.get(name);
                    } else {
                        conversable = new ServerUser(sender, host, user, server);
                    }
                    server.eventManager.fireEvent(new PrivateMessageEvent(new PRIVMSG(msg, sender, conversable), this));
                } else if (message.getCommand().equals("NOTICE")) {
                    String msg = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
                    String sender = s.split("!")[0];
                    String channel = null;
                    if (s.contains("NOTICE #"))
                        channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
                    server.eventManager.fireEvent(new NoticeEvent(new NOTICE(msg, sender, channel), this));

                } else if (message.getCommand().equals(Numerics.SERVER_SUPPORT)) {
                    if (s.contains("PREFIX")) {
                        String temp = s.substring(s.indexOf("PREFIX"));
                        temp = temp.substring(0, temp.indexOf(" ")).replace("PREFIX=", "");
                        String letters = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
                        String symbols = temp.substring(temp.indexOf(")") + 1);
                        if (letters.length() == symbols.length()) {
                            server.setModeLetters(letters.toCharArray());
                            server.setModeSymbols(symbols.toCharArray());
                        }
                    }
                } else if (code.equals("KICK")) {
                    final Channel channel = server.channels.get(raw.split(" ")[2]);
                    if (channel == null) {
                        continue;
                    }
                    final ChannelUser user = channel.getUser(raw.split(" ")[3]);
                    if (user == null) {
                        continue;
                    }

                    server.getEventManager().fireEvent(
                            new ChannelUserEvent(this, channel, user, ChannelUserEvent.USER_KICKED));
                } else if (code.equals("PART")) {
                    final String nick = message.getSender().split("!")[0];
                    Channel channel = server.channels.get(raw.split(" ")[2]);
                    if (channel == null) {
                        channel = new Channel(raw.split(" ")[2], server);
                    }
                    final ChannelUser user = channel.getUser(nick);
                    server.getEventManager().fireEvent(new ChannelUserEvent(this, channel, user, ChannelUserEvent.USER_PARTED));
                } else if (code.equals("JOIN")) {
                    final String nick = raw.split("!")[0];
                    final String user = raw.substring(raw.indexOf("!") + 1, raw.indexOf("@"));
                    final String host = raw.substring(raw.indexOf("@") + 1, raw.indexOf("JOIN")).trim();
                    Channel channel = server.channels.get(raw.split(" ")[2].replace(":", ""));
                    if (channel == null) {
                        channel = new Channel(raw.split(" ")[2], server);
                    }
                    final ChannelUser u = new ChannelUser(nick, "", user, host, channel);
                    server.getEventManager().fireEvent(new ChannelUserEvent(this, channel, u, ChannelUserEvent.USER_JOINED));
                } else if (code.equals("MODE")) {
                    String name = raw.split(" ")[2];
                    if (!server.channels.containsKey(name)) {
                        continue;
                    }
                    Channel channel = server.channels.get(name);

                    raw = raw.substring(raw.indexOf(code));
                    raw = raw.substring(raw.indexOf(name) + name.length()).trim();
                    String[] strings = raw.split(" ");
                    String modes = strings[0];
                    if (strings.length == 1) {
                        channel.chanMode.parse(modes);
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
                                continue;
                            }
                            ChannelUser user = channel.getUser(u[index]);
                            if (user != null) {
                                if (plus) {
                                    user.addMode(c);
                                } else {
                                    user.removeMode(c);
                                }
                                server.getEventManager().fireEvent(
                                        new ChannelUserEvent(this, channel, user, ChannelUserEvent.USER_MODE_CHANGED));

                            }
                        }
                        index++;


                    }


                } else if (code.equals(Numerics.WHO_RESPONSE)) {
                    Channel channel = server.channels.get(raw.split(" ")[3]);
                    String[] temp = raw.split(" ");
                    String user = temp[4];
                    String host = temp[5];
                    String nick = temp[7];
                    String modes = temp[8];
                    modes = modes.replace("*", "").replace("G", "").replace("H", "");
                    channel.tempList.add(new ChannelUser(nick, modes, user, host, channel));

                } else if (code.equals(Numerics.WHO_END)) {
                    Channel channel = server.channels.get(raw.split(" ")[3]);

                    channel.users.clear();
                    channel.users.addAll(channel.tempList);
                    channel.tempList.clear();
                } else if (code.toLowerCase().equals("topic")) {
                    Channel channel = server.channels.get(raw.split(" ")[2]);
                    String[] temp = raw.split(" :", 2);
                    if (temp[0].substring(temp[0].indexOf("TOPIC")).contains(channel.getName())) {
                        channel.setTopic(temp[1]);
                    }
                } else if (code.equals(Numerics.BANNED_FROM_CHANNEL) && message.getTarget().equals(server.getNick())) {
                    Channel channel = server.channels.get(raw.split(" ")[3]);
                    if (channel != null && channel.isRunning) channel.isRunning = false;
                } else if (code.equals("NICK")) {
                    for (Channel channel : server.channels.values()) {
                        final ChannelUser user = channel.getUser(message.getSender().split("!")[0]);
                        if (user != null) {
                            user.setNick(raw.substring(raw.indexOf(": ") + 2).trim());
                        }
                    }
                }
                server.eventManager.fireEvent(new RawMessageEvent(message, this));
            }

            server.socket.close();
            server.eventManager.setRunning(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}