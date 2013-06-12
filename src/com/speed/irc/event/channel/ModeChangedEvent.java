package com.speed.irc.event.channel;

import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;
import com.speed.irc.types.ModeList;

/**
 * A class representing the change of modes in a Channel.
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
public class ModeChangedEvent extends ChannelEvent {
    private ChannelUser affectedUser;
    private ModeList modes;
    private String affectedMask;

    public ModeChangedEvent(Channel channel, String senderNick, Object source, String... args) {
        super(channel, ChannelEvent.MODE_CHANGED, senderNick, source, args);
        modes = new ModeList(channel.getServer(), args[0]);
        affectedMask = args.length == 1 ? channel.getName() : args[1];
    }

    public ModeChangedEvent(Channel channel, ChannelUser affectedUser, String senderNick, Object source, String... args) {
        super(channel, ChannelEvent.MODE_CHANGED, senderNick, source, args);
        this.affectedUser = affectedUser;
        this.modes = new ModeList(channel.getServer(), args[0]);
    }

    public ModeList getNewModes() {
        return modes;
    }

    public ChannelUser getAffectedUser() {
        return affectedUser;
    }

    public String getAffectedMask() {
        return affectedMask;
    }


}
