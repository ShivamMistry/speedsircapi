package com.speed.irc.event.generators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.speed.irc.event.EventGenerator;
import com.speed.irc.event.IRCEvent;
import com.speed.irc.event.NoticeEvent;
import com.speed.irc.types.NOTICE;
import com.speed.irc.types.RawMessage;

/**
 * Processes NOTICE messages sent from the server.
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
public class NoticeGenerator implements EventGenerator {
	private static final Pattern PATTERN_NOTICE = Pattern
			.compile("(.+?)!(.+?)@(.+?) NOTICE (#?.+?) :(.*)");

	public boolean accept(RawMessage raw) {
		return PATTERN_NOTICE.matcher(raw.getRaw()).matches();
	}

	public IRCEvent generate(RawMessage raw) {
		final Matcher notice_matcher = PATTERN_NOTICE.matcher(raw.getRaw());
		if (notice_matcher.matches()) {
			final String msg = notice_matcher.group(5);
			final String sender = notice_matcher.group(1);
			final String name = notice_matcher.group(4);
			String channel = null;
			if (raw.getRaw().split(" :", 2)[0].contains("NOTICE #"))
				channel = name;
			return new NoticeEvent(new NOTICE(msg, sender, channel), this);
		}
		return null;
	}

}
