package com.speed.irc.connection;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.speed.irc.event.ChannelEvent;
import com.speed.irc.event.ChannelUserEvent;
import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.ExceptionEvent;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.event.generators.JoinGenerator;
import com.speed.irc.event.generators.KickGenerator;
import com.speed.irc.event.generators.ModeGenerator;
import com.speed.irc.event.generators.NoticeGenerator;
import com.speed.irc.event.generators.PartGenerator;
import com.speed.irc.event.generators.PrivmsgGenerator;
import com.speed.irc.event.generators.WhoisGenerator;
import com.speed.irc.types.CTCPReply;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.ParsingException;
import com.speed.irc.types.RawMessage;
import com.speed.irc.types.ServerUser;
import com.speed.irc.util.Numerics;

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
 * @author Shivam Mistry
 */
public class ServerMessageParser implements Runnable, EventGenerator {
	private final Server server;
	private List<EventGenerator> generators;
	protected ServerMessageReader reader;
	protected ScheduledExecutorService execServ;
	protected Future<?> future;

	public static final CTCPReply CTCP_REPLY_VERSION = new CTCPReply() {

		public String getReply() {
			return "Speed's IRC API";
		}

		public String getRequest() {
			return "VERSION";
		}

	};

	public static final CTCPReply CTCP_REPLY_TIME = new CTCPReply() {

		public String getReply() {
			return new Date().toString();
		}

		public String getRequest() {
			return "TIME";
		}

	};

	public static final CTCPReply CTCP_REPLY_PING = new CTCPReply() {

		public String getReply() {
			return "";
		}

		public String getRequest() {
			return "PING (.*)";
		}

	};

	public ServerMessageParser(final Server server) {
		this.server = server;
		generators = new CopyOnWriteArrayList<EventGenerator>();
		generators.add(this);
		generators.add(new JoinGenerator());
		generators.add(new KickGenerator());
		generators.add(new ModeGenerator());
		generators.add(new NoticeGenerator(server));
		generators.add(new PartGenerator());
		generators.add(new PrivmsgGenerator());
		generators.add(new WhoisGenerator(server));
		reader = new ServerMessageReader(server);
		execServ = Executors.newSingleThreadScheduledExecutor();
		new Thread(reader, "Server message reader").start();
		future = execServ.scheduleWithFixedDelay(this, 0, 20,
				TimeUnit.MILLISECONDS);

	}

	private synchronized void parse(final String s) throws Exception {
		final RawMessage message = new RawMessage(s, server);
		for (EventGenerator generator : generators) {
			if (generator.accept(message)) {
				IRCEvent event = generator.generate(message);
				if (event != null)
					server.eventManager.dispatchEvent(event);
			}
		}
		server.eventManager.dispatchEvent(new RawMessageEvent(message, this));

	}

	/**
	 * Submits an event generator to this parser
	 * 
	 * @param generator
	 *            generator to add
	 */
	public void addGenerator(final EventGenerator generator) {
		generators.add(generator);
	}

	/**
	 * Removes a generator from this parser
	 * 
	 * @param generator
	 *            the generator to remove
	 * @return true if it was removed, false if it failed to be removed
	 */
	public boolean removeGenerator(final EventGenerator generator) {
		return generators.remove(generator);
	}

	public void run() {
		String s;
		if (!reader.isEmpty()) {
			s = reader.poll();
			if (s.matches("\\W.+"))
				s = s.substring(1);
			try {
				parse(s);
			} catch (Exception e) {
				server.eventManager
						.dispatchEvent(new ExceptionEvent(new ParsingException(
								"Parsing error", e), this, server));
			}
		}

	}

	public boolean accept(RawMessage message) {
		return message != null;
	}

	public IRCEvent generate(RawMessage message) {
		String raw = message.getRaw();
		String code = message.getCommand().trim();
		if (raw.startsWith("PING")) {
			server.sendRaw(raw.replaceFirst("PING", "PONG") + "\n");
		} else if (message.getCommand().equals(Numerics.SERVER_SUPPORT)) {
			if (raw.contains("PREFIX")) {
				String temp = raw.substring(0, raw.indexOf(" :"));
				String[] parts = temp.split(" ");
				for (String t : parts) {
					if (t.startsWith("PREFIX=")) {
						String letters = t.split("\\(", 2)[1].split("\\)")[0];
						String symbols = t.split("\\)", 2)[1];
						if (letters.length() == symbols.length()) {
							server.setModeLetters(letters.toCharArray());
							server.setModeSymbols(symbols.toCharArray());
						}
					}
				}
			}
		} else if (code.equals(Numerics.CHANNEL_MODES)) {
			String chan_name = message.getRaw().split(" ")[3];
			String modez = message.getRaw().split(" ")[4];
			if (!server.channels.containsKey(chan_name)) {
				return null;
			}
			Channel channel = server.channels.get(chan_name);
			channel.chanMode.parse(modez);
		} else if (code.equals(Numerics.CHANNEL_NAMES)) {
			String[] parts = message.getRaw().split(" ");
			// String secret = parts[3];
			String chan_name = parts[4];
			String users = message.getRaw().split(" :")[1];
			if (!server.channels.containsKey(chan_name)) {
				return null;
			}
			Channel channel = server.channels.get(chan_name);
			if (channel.isRunning()) {
				for (String s : users.split(" ")) {
					if (s.matches("[A-Za-z].*")) {
						channel.userBuffer.add(new ChannelUser(s, "", "", "",
								channel));
					} else {
						char c = s.charAt(0);
						channel.userBuffer.add(new ChannelUser(s.substring(1),
								Character.toString(c), "", "", channel));
					}
				}
			}
		} else if (code.equals(Numerics.CHANNEL_NAMES_END)) {
			Channel channel = server.channels.get(raw.split(" ")[3]);
			channel.getUsers().clear();
			channel.getUsers().addAll(channel.userBuffer);
			channel.userBuffer.clear();
		} else if (code.equals(Numerics.WHO_RESPONSE)) {
			Channel channel = server.channels.get(raw.split(" ")[3]);
			String[] temp = raw.split(" ");
			String user = temp[4];
			String host = temp[5];
			String nick = temp[7];
			String modes = temp[8];
			boolean away = modes.contains("G");
			boolean oper = modes.contains("*");
			boolean identified = modes.contains("r");
			modes = modes.replace("*", "").replace("G", "").replace("H", "")
					.replace("r", "");
			modes = modes.replace("r", "");
			ChannelUser u = new ChannelUser(nick, modes, user, host, channel);
			u.setIdentified(identified);
			u.setAway(away);
			u.setOper(oper);
			channel.userBuffer.add(u);

		} else if (code.equals(Numerics.WHO_END)) {
			Channel channel = server.channels.get(raw.split(" ")[3]);
			channel.users.clear();
			channel.users.addAll(channel.userBuffer);
			channel.userBuffer.clear();
		} else if (code.toLowerCase().equals("topic")) {
			Channel channel = server.getChannel(raw.split(" ")[2]);
			if (!channel.isRunning()) {
				channel.setup();
			}
			String topicSetter = raw.split(" ")[0];
			long time = System.currentTimeMillis();
			channel.setTopicSetter(topicSetter);
			channel.setTopicSetTime(time);
			String[] temp = raw.split(" :", 2);
			channel.setTopic(temp[1]);
			if (temp[0].substring(temp[0].indexOf("TOPIC")).contains(
					channel.getName())) {
				return new ChannelEvent(channel, ChannelEvent.TOPIC_CHANGED,
						this);
			}
		} else if (code.equals(Numerics.BANNED_FROM_CHANNEL)
				&& message.getTarget().equals(server.getNick())) {
			Channel channel = server.channels.get(raw.split(" ")[3]);
			if (channel != null && channel.isRunning())
				channel.isRunning = false;
		} else if (code.equals("QUIT")) {
			String nick = raw.split("!")[0];
			for (Channel c : server.channels.values()) {
				ChannelUser u = c.getUser(nick);
				if (u != null) {
					c.removeChannelUser(u);
				}
			}
			// TODO: quit event.
		} else if (code.trim().equalsIgnoreCase("nick")) {
			try {
				final ServerUser u = server.getUser(message.getSender().split(
						"!")[0]);
				final String oldNick = u.getNick();
				final String newNick = raw.split(" :")[1].trim();
				if (oldNick.equals(server.getNick())) {
					server.putNick(newNick);
				}

				if (u instanceof ChannelUser) {
					return new ChannelUserEvent(this,
							((ChannelUser) u).getChannel(), (ChannelUser) u,
							ChannelUserEvent.USER_NICK_CHANGED, new String[] {
									oldNick, newNick });
				} else {
					// this user is not in a channel, so we need to recreate the
					// object
					ServerUser n_u = new ServerUser(newNick, u.getHost(),
							u.getUser(), server);
					server.removeUser(u);
					server.addUser(n_u);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		parseNumerics(message);
		return null;
	}

	private void parseNumerics(RawMessage message) {
		String code = message.getCommand();
		if (code.equals(Numerics.CHANNEL_TOPIC)) {
			String chanName = message.getRaw().split(" ")[3];
			String topic = message.getRaw().split(" :", 2)[1].trim();
			Channel c = server.getChannel(chanName);
			if (!c.isRunning()) {
				c.setup();
			}
			c.setTopic(topic);

		} else if (code.equals(Numerics.CHANNEL_TOPIC_SET)) {
			String[] parts = message.getRaw().split(" ");
			String chanName = parts[3];
			String setter = parts[4];
			/*
			 * if(setter.contains("!")) { setter = setter.split("!")[0]; }
			 */
			// would use the above code but we want the api to capture as much
			// info as possible
			// some servers send the mask, some just send the nick
			String timestamp = parts[5];
			Channel c = server.getChannel(chanName);
			if (!c.isRunning()) {
				c.setup();
			}
			c.setTopicSetter(setter);
			c.setTopicSetTime(Long.parseLong(timestamp) * 1000L);
		}
	}
}