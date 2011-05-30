package com.speed.irc.connection;

import com.speed.irc.event.EventManager;
import com.speed.irc.event.NoticeEvent;
import com.speed.irc.event.PrivateMessageEvent;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.types.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class representing a socket connection to an IRC server with the
 * functionality of sending raw commands and messages.
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
public class Server implements ConnectionHandler, Runnable {
    private BufferedWriter write;
    private BufferedReader read;
    private final Socket socket;
    private EventManager eventManager = new EventManager();
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
    private BlockingQueue<String> whoQueue = new LinkedBlockingQueue<String>();
    private Map<String, Channel> channels = new HashMap<String, Channel>();
    private char[] modeSymbols;
    private char[] modeLetters;
    private String serverName;

    public Server(final Socket sock) throws IOException {
        socket = sock;
        setServerName(socket.getInetAddress().getHostAddress());
        write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
        read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        Thread eventThread = new Thread(eventManager);
        eventThread.start();
        new Thread(this).start();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                String s;
                try {
                    while ((s = read.readLine()) != null) {
                        s = s.substring(1);
                        final RawMessage message = new RawMessage(s);
                        if (s.startsWith("PING")) {
                            sendRaw("PONG" + s.replaceFirst("PING", "") + "\n");
                        } else if (message.getCommand().equals("PRIVMSG")) {
                            final String msg = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
                            final String sender = message.getSender().split("!")[0];

                            if (msg.equals("\u0001VERSION\u0001")) {
                                sendRaw("NOTICE " + sender + " :\u0001VERSION Speed's IRC API\u0001\n");
                            }
                            String channel = null;
                            if (s.contains("PRIVMSG #")) {
                                channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
                            }
                            eventManager.fireEvent(new PrivateMessageEvent(new PRIVMSG(msg, sender, channels
                                    .get(channel)), this));
                        } else if (message.getCommand().equals("NOTICE")) {
                            String msg = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
                            String sender = s.split("!")[0];
                            String channel = null;
                            if (s.contains("NOTICE #"))
                                channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
                            eventManager.fireEvent(new NoticeEvent(new NOTICE(msg, sender, channel), this));

                        } else if (message.getCommand().equals(Numerics.SERVER_SUPPORT)) {
                            if (s.contains("PREFIX")) {
                                String temp = s.substring(s.indexOf("PREFIX"));
                                temp = temp.substring(0, temp.indexOf(" ")).replace("PREFIX=", "");
                                String letters = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")"));
                                String symbols = temp.substring(temp.indexOf(")") + 1);
                                if (letters.length() == symbols.length()) {
                                    modeLetters = letters.toCharArray();
                                    modeSymbols = symbols.toCharArray();
                                }
                            }
                        }
                        eventManager.fireEvent(new RawMessageEvent(message, this));
                    }

                    socket.close();
                    eventManager.setRunning(false);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }

    /**
     * Sends a message to a channel/nick
     *
     * @deprecated See {@link Channel#sendMessage(String)}
     */
    public void sendMessage(final PRIVMSG message) {
        sendRaw("PRIVMSG " + message.getChannel().getName() + " :" + message.getMessage() + "\n");
    }

    /**
     * Sends a raw command to the server.
     *
     * @param raw The raw command to be added to the sending queue.
     */
    public void sendRaw(final String raw) {
        messageQueue.add(raw);
    }

    /**
     * Gets the channel map.
     *
     * @return the channel map.
     */
    public Map<String, Channel> getChannels() {
        return channels;
    }

    public void addToWhoQueue(String s) {
        whoQueue.add(s);
    }

    /**
     * Gets the buffered writer.
     *
     * @return the buffered writer.
     */
    public BufferedWriter getWriter() {
        return write;
    }

    /**
     * Sets the buffered writer.
     *
     * @param write the new buffered writer.
     */
    public void setWrite(final BufferedWriter write) {
        this.write = write;
    }

    /**
     * Gets the buffered reader.
     *
     * @return the buffered reader.
     */
    public BufferedReader getReader() {
        return read;
    }

    /**
     * Sets the buffered reader.
     *
     * @param read the new buffered reader.
     */
    public void setRead(final BufferedReader read) {
        this.read = read;
    }

    /**
     * Gets the channel access mode symbols (e.g. @ for op)
     *
     * @return the channel access mode symbols.
     */
    public char[] getModeSymbols() {
        return modeSymbols;
    }

    public void setModeSymbols(final char[] modeSymbols) {
        this.modeSymbols = modeSymbols;
    }

    /**
     * Gets the channel access mode letters (e.g. v for voice)
     *
     * @return the channel access mode letters
     */
    public char[] getModeLetters() {
        return modeLetters;
    }

    public void setModeLetters(final char[] modeLetters) {
        this.modeLetters = modeLetters;
    }

    /**
     * Sends a notice to the specified nick.
     *
     * @param notice sender can be null.
     */
    public void sendNotice(final NOTICE notice) {
        sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage() + "\n");
    }

    /**
     * Joins a channel.
     *
     * @param channel The channel you wish to join.
     */
    public void joinChannel(String channel) {
        sendRaw("JOIN " + channel + "\n");
    }

    /**
     * Sends an action to a channel/nick.
     *
     * @param channel The specified nick you would like to send the action to.
     * @param action  The action you would like to send.
     */
    public void sendAction(final String channel, final String action) {
        sendRaw("PRIVMSG " + channel + ": \u0001ACTION " + action + "\u0001\n");
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void run() {
        while (socket.isConnected()) {

            String s = messageQueue.poll();

            if (s != null) {
                if (!s.endsWith("\n")) {
                    s = s + "\n";
                }
                try {
                    if (write != null) {
                        write.write(s);
                        write.flush();
                    } else {
                        messageQueue.add(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String who = whoQueue.poll();
            if (who != null) {

            }
        }
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Gets the server's host address.
     *
     * @return the server's host address.
     */
    public String getServerName() {
        return serverName;
    }

}
