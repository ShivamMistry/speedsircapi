package com.speed.irc.script;

import java.util.Random;

import com.speed.irc.types.Bot;
import com.speed.irc.types.NOTICE;
import com.speed.irc.types.PRIVMSG;

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
public class HelloBot extends Bot {

	public static final String[] HELLO_PHRASES = new String[] { "Hello", "Hi", "Hey", "Yo", "Wassup", "helo", "herro",
			"hiya", "hai", "heya" };
	public static final Random random = new Random();
	public static final String CHANNEL = "#IRC";
	public long lastMessage = System.currentTimeMillis();

	public HelloBot(final String server, final int port) {
		super(server, port);
	}

	public static void main(String[] args) {
		new HelloBot("irc.strictfp.com", 6667);
	}

	public void messageReceived(final PRIVMSG msg) {
		String message = msg.getMessage();
		String sender = msg.getSender();
		for (String s : HELLO_PHRASES) {
			if (message.toLowerCase().equals(s.toLowerCase())
					|| (message.contains("London") && message.toLowerCase().contains(s.toLowerCase()))) {
				connection.sendMessage(new PRIVMSG(HELLO_PHRASES[random.nextInt(HELLO_PHRASES.length - 1)] + " "
						+ sender, null, CHANNEL));
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				lastMessage = System.currentTimeMillis();
			}
		}

		if (message.contains("!raw") && sender.equals("Speed")) {
			connection.sendRaw(message.replaceFirst("!raw", "").trim() + "\n");
		}

	}

	public void rawMessageReceived(final String raw) {
		System.out.println("HelloBot: " + raw);
		if (raw.contains("JOIN " + CHANNEL) || raw.contains("JOIN :" + CHANNEL)) {
			String sender = raw.split("!")[0].replaceFirst(":", "");
			connection.sendMessage(new PRIVMSG(HELLO_PHRASES[random.nextInt(HELLO_PHRASES.length - 1)] + " " + sender,
					null, CHANNEL));
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public String[] getChannels() {
		return new String[] { CHANNEL };
	}

	public String getNick() {
		return "London";
	}

	@SuppressWarnings("deprecation")
	public void onStart() {
		try {
			identify("password");
			setAutoRejoin(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void noticeReceived(final NOTICE notice) {
		System.out.println(notice.getMessage());
	}

}
