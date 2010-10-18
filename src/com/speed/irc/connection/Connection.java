package com.speed.irc.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.speed.irc.types.NOTICE;
import com.speed.irc.types.PRIVMSG;

/**
 * A class representing a socket connection to an irc server with the
 * functionality of sending raw commands and messages.
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
public class Connection {
	public BufferedWriter write;
	public BufferedReader read;

	public Connection(Socket sock) throws IOException {
		write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		read = new BufferedReader(new InputStreamReader(sock.getInputStream()));

	}

	/**
	 * Sends a message to a channel/nick
	 * 
	 * @param CHANNEL
	 *            Either the channel or nick you wish to send the message to.
	 * @param message
	 *            The message you wish to send
	 * 
	 */
	public void sendMessage(final PRIVMSG message) {
		sendRaw("PRIVMSG " + message.getChannel() + " :" + message.getSender() + "\n");
	}

	/**
	 * Sends a raw command to the server, remember to use "\n" for a new line
	 * everytime you send a raw command.
	 * 
	 * @param raw
	 *            The raw command you wish to send to the server.
	 */
	public void sendRaw(String raw) {
		try {
			write.write(raw);
			write.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a notice to the specified nick.
	 * 
	 * @param to
	 *            The nick/channel you wish to send the message to.
	 * @param message
	 *            The message you wish to send.
	 */
	public void sendNotice(final NOTICE notice) {
		sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage() + "\n");
	}

	/**
	 * Joins a channel.
	 * 
	 * @param channel
	 *            The channel you wish to join.
	 */
	public void joinChannel(String channel) {
		sendRaw("JOIN " + channel + "\n");
	}

	/**
	 * Sends an action to a channel/nick.
	 * 
	 * @param channel
	 *            The specified nick you would like to send the action to.
	 * @param action
	 *            The action you would like to send.
	 */
	public void sendAction(String channel, String action) {
		sendRaw("PRIVMSG " + channel + ": ACTION " + action + "");
	}

}
