package com.speed.irc.script;

import java.util.Random;

import com.speed.irc.event.PrivateMessageEvent;
import com.speed.irc.event.PrivateMessageListener;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.event.RawMessageListener;
import com.speed.irc.types.Bot;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

/**
 * Greets people as they join the channel or speak a greeting.
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
public class HelloBot extends Bot implements RawMessageListener, PrivateMessageListener {

	public static final String[] HELLO_PHRASES = new String[] { "Hello", "Hi", "Hey", "Yo", "Wassup", "helo", "herro",
			"hiya", "hai", "heya" };
	public static final Random random = new Random();
	public Channel CHANNEL;
	public long lastMessage = System.currentTimeMillis();

	public HelloBot(final String server, final int port) {
		super(server, port);
	}

	public static void main(String[] args) {
		new HelloBot("irc.strictfp.com", 6667);
	}

	public void rawMessageReceived(final RawMessageEvent e) {
		String raw = e.getMessage().getRaw();
		String code = e.getMessage().getCommand();
		if (code.equals("JOIN")) {
			String sender = raw.split("!")[0].replaceFirst(":", "");
			CHANNEL.sendMessage(HELLO_PHRASES[random.nextInt(HELLO_PHRASES.length - 1)] + " " + sender);

		}

	}

	public Channel[] getChannels() {
		CHANNEL = new Channel("#rscode", connection, getNick());
		return new Channel[] { CHANNEL };
	}

	public String getNick() {
		return "London";
	}

	public void onStart() {
		try {
			// identify("password");
			CHANNEL.setAutoRejoin(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void messageReceived(PrivateMessageEvent e) {
		String message = e.getMessage().getMessage();
		String sender = e.getMessage().getSender();

		if (message.contains("!raw") && sender.equals("Speed")) {
			connection.sendRaw(message.replaceFirst("!raw", "").trim() + "\n");
		}
		if (e.getMessage().getChannel() == null) {
			return;
		}
		for (ChannelUser u : e.getMessage().getChannel().getUsers()) {
			if (u.getNick().equals(sender)) {
				for (String s : HELLO_PHRASES) {
					if (message.toLowerCase().equals(s.toLowerCase())
							|| (message.contains("London") && message.toLowerCase().contains(s.toLowerCase()))) {
						CHANNEL.sendMessage(HELLO_PHRASES[random.nextInt(HELLO_PHRASES.length - 1)] + " " + sender
								+ " with rights: " + u.getRights());
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						lastMessage = System.currentTimeMillis();
					}
				}
			}
		}
	}

}
