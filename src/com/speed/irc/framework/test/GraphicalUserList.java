package com.speed.irc.framework.test;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.speed.irc.framework.Bot;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

/**
 * Displays a visual list of all the users in the channel, and associated
 * properties.
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
public class GraphicalUserList extends JFrame implements Runnable {

	private static final long serialVersionUID = -5395939612572800357L;
	private Channel mainChannel;
	private JList<ChannelUser> list;

	public static void main(String[] args) {
		new GraphicalUserList();
	}

	public GraphicalUserList() {
		new Bot("irc.rizon.net", 6697, true) {

			public void onStart() {
				mainChannel = new Channel("#speedsircapi", getServer());
				getServer().setReadDebug(true);
			}

			public Channel[] getChannels() {
				return new Channel[] { mainChannel };
			}

			public String getNick() {
				return "UserLister";
			}

		};
		setSize(600, 300);
		setTitle("User List");
		@SuppressWarnings("serial")
		AbstractListModel<ChannelUser> model = new AbstractListModel<ChannelUser>() {

			public ChannelUser getElementAt(int i) {
				return mainChannel.getSortedUsers()[i];
			}

			@Override
			public int getSize() {
				// TODO Auto-generated method stub
				return mainChannel.getUsers().size();
			}

		};

		list = new JList<ChannelUser>(model);
		list.setFixedCellHeight(15);
		JScrollPane pane = new JScrollPane(list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(pane);
		setVisible(true);
		new Thread(this).start();
	}

	public void run() {
		while (isVisible()) {
			repaint();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
