package com.speed.irc.types;

import com.speed.irc.connection.Server;
import com.speed.irc.event.ChannelUserEvent;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.event.RawMessageListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a channel
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
public class Channel implements RawMessageListener, Runnable {
    protected String name;
    protected Server server;
    protected List<ChannelUser> users = new LinkedList<ChannelUser>();
    protected List<ChannelUser> tempList = new LinkedList<ChannelUser>();
    public volatile boolean isRunning = true;
    public static final int WHO_DELAY = 90000;
    protected boolean autoRejoin;
    protected String nick;
    protected Mode chanMode;
    protected List<String> bans = new LinkedList<String>();
    protected String topic;
    protected Thread channel;

    /**
     * Constructs a channel.
     *
     * @param name   the name of the channel.
     * @param server the server object this channel is associated with.
     * @param nick   the nick of the client.
     */
    public Channel(final String name, final Server server, final String nick) {
        this.name = name;
        this.server = server;
        this.nick = nick;
        this.server.getChannels().put(name, this);
        this.server.getEventManager().addListener(this);
        chanMode = new Mode(server, "");
    }

    /**
     * Gets the name of the channel.
     *
     * @return the name of the channel
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of users in the channel.
     *
     * @return The list of users in the channel.
     */
    public List<ChannelUser> getUsers() {
        return users;
    }

    /**
     * Gets a user from the channel.
     *
     * @param nick The nick of the ChannelUser to get.
     * @return The ChannelUser object associated with the nick or
     *         <code>null</code>.
     */
    public ChannelUser getUser(final String nick) {
        for (ChannelUser user : users) {
            if (user.getNick().equals(nick)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Sets whether rejoining is enabled when kicked
     *
     * @param on turn auto-rejoin on or not
     */
    public void setAutoRejoin(final boolean on) {
        autoRejoin = on;
    }

    /**
     * Leaves the channel.
     *
     * @param message The part message, can be null for no message.
     */
    public void part(final String message) {
        isRunning = false;
        if (message == null)
            server.sendRaw(String.format("PART %s :%s\n", name, message));
        else
            server.sendRaw(String.format("PART %s\n", name));
    }

    /**
     * Sends a message to the channel.
     *
     * @param message The message to be sent
     */
    public void sendMessage(final String message) {
        server.sendRaw(String.format("PRIVMSG %s :%s\n", name, message));
    }

    public void rawMessageReceived(final RawMessageEvent e) {
        String raw = e.getMessage().getRaw();
        String code = e.getMessage().getCommand();
        if (code.equals("KICK") && raw.split(" ")[3].equals(nick)) {
            final ChannelUser user = getUser(nick);
            if (user != null) {
                users.remove(user);
            }
            if (autoRejoin)
                join();
            else
                isRunning = false;
            server.getEventManager().fireEvent(
                    new ChannelUserEvent(this, this, getUser(raw.split(" ")[3]), ChannelUserEvent.USER_KICKED));
        } else if (code.equals("KICK")) {
            final String nick = raw.split(" ")[3];
            final ChannelUser user = getUser(nick);
            if (user != null) {
                users.remove(user);
            }
            server.getEventManager().fireEvent(new ChannelUserEvent(this, this, user, ChannelUserEvent.USER_KICKED));
        } else if (code.equals("PART")) {
            final String nick = e.getMessage().getSender().split("!")[0];
            final ChannelUser user = getUser(nick);
            if (user != null) {
                users.remove(user);
            }
            server.getEventManager().fireEvent(new ChannelUserEvent(this, this, user, ChannelUserEvent.USER_PARTED));
        } else if (code.equals("JOIN")) {
            final String nick = raw.split("!")[0];
            final String user = raw.substring(raw.indexOf("!") + 1, raw.indexOf("@"));
            final String host = raw.substring(raw.indexOf("@") + 1, raw.indexOf("J")).trim();
            final ChannelUser u = new ChannelUser(nick, "", user, host, this);
            users.add(u);
            server.getEventManager().fireEvent(new ChannelUserEvent(this, this, u, ChannelUserEvent.USER_JOINED));
        } else if (code.equals("MODE")) {
            raw = raw.substring(raw.indexOf(code));
            if (raw.contains(name)) {
                raw = raw.substring(raw.indexOf(name) + name.length()).trim();
                String[] strings = raw.split(" ");
                String modes = strings[0];
                if (strings.length == 1) {
                    chanMode.parse(modes);
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
                        index++;
                        if (c == 'b') {
                            if (plus) {
                                bans.add(u[index]);
                            } else {
                                bans.remove(u[index]);
                            }
                            continue;
                        }
                        for (ChannelUser user : users) {
                            if (user.getNick().equals(u[index])) {
                                if (plus) {
                                    user.addMode(c);
                                } else {
                                    user.removeMode(c);
                                }
                                server.getEventManager().fireEvent(
                                        new ChannelUserEvent(this, this, user, ChannelUserEvent.USER_MODE_CHANGED));

                            }
                        }

                    }

                }
            }
        } else if (code.equals(Numerics.WHO_RESPONSE)) {
            if (raw.contains(name)) {
                String[] temp = raw.split(" ");
                String user = temp[4];
                String host = temp[5];
                String nick = temp[7];
                String modes = temp[8];
                modes = modes.replace("*", "").replace("G", "").replace("H", "");
                tempList.add(new ChannelUser(nick, modes, user, host, this));
            }
        } else if (code.equals(Numerics.WHO_END)) {
            users.clear();
            users.addAll(tempList);
            tempList.clear();
        } else if (code.toLowerCase().equals("topic")) {
            String[] temp = raw.split(" :", 2);
            if (temp[0].substring(temp[0].indexOf("TOPIC")).contains(name)) {
                setTopic(temp[1]);
            }
        } else if (code.equals(Numerics.BANNED_FROM_CHANNEL) && e.getMessage().getTarget().equals(nick)) {
            isRunning = false;
        } else if (code.equals("NICK")) {
            final ChannelUser user = getUser(e.getMessage().getSender().split("!")[0]);
            if (user != null) {
                user.setNick(raw.substring(raw.indexOf(": ") + 2).trim());
            }
        }

    }

    public void run() {
        do {
            server.sendRaw("WHO " + name);
            if (!users.isEmpty())
                try {
                    Thread.sleep(Channel.WHO_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (isRunning);
    }

    /**
     * Joins the channel.
     */
    public void join() {
        server.joinChannel(name);

        isRunning = true;
        if (channel == null || !channel.isAlive()) {
            channel = new Thread(this);
            channel.start();
        }
    }

    /**
     * Bans then kicks the channel user with the reason specified.
     *
     * @param user   the ChannelUser to kick.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kickBan(final ChannelUser user, final String reason) {
        ban(user);
        kick(user, reason);
    }

    /**
     * Attempts to ban the specified ChannelUser.
     *
     * @param user the user that should be banned.
     */
    public void ban(final ChannelUser user) {
        final String banMask = new StringBuffer().append("*!*@").append(user.getHost()).toString();
        ban(banMask);
    }

    /**
     * Attempts to ban the specified mask.
     *
     * @param banMask The ban-mask that should be banned.
     */
    public void ban(final String banMask) {
        server.sendRaw(String.format("MODE %s +b %s\n", name, banMask));
    }

    /**
     * Attempts to kick a channel user.
     *
     * @param user   The ChannelUser that is to be kicked.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kick(final ChannelUser user, String reason) {
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(), reason));
    }

    /**
     * Attempts to kick a channel user.
     *
     * @param nick   The nick of the user that is to be kicked.
     * @param reason The reason for kicking the channel user, can be
     *               <code>null</code>.
     */
    public void kick(final String nick, String reason) {
        final ChannelUser user = getUser(nick);
        if (user == null) {
            return;
        }
        if (reason == null) {
            reason = user.getNick();
        }
        server.sendRaw(String.format("KICK %s %s :%s\n", name, user.getNick(), reason));
    }

    /**
     * Sets the channel's topic. Attempts to send any changes to the server.
     *
     * @param topic The new channel topic.
     */
    public void setTopic(final String topic) {
        server.sendRaw(String.format("TOPIC %s :%s\n", name, topic));
    }

    /**
     * Gets the topic.
     *
     * @return the channel's topic
     */
    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Channel && ((Channel) o).getName().equals(getName());
    }

}