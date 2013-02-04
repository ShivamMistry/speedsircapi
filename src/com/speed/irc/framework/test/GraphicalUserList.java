package com.speed.irc.framework.test;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.speed.irc.framework.Bot;
import com.speed.irc.types.Channel;
import com.speed.irc.types.ChannelUser;

public class GraphicalUserList extends JFrame implements Runnable {

	private static final long serialVersionUID = -5395939612572800357L;
	private Channel mainChannel;
	private JList<ChannelUser> list;

	public static void main(String[] args) {
		new GraphicalUserList();
	}

	public GraphicalUserList() {
		new Bot("irc.rizon.net", 6667) {

			public void onStart() {
				mainChannel = new Channel("#rscode", getServer());
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
