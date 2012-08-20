package com.speed.irc.types;

public class Mask {

	private String mask;

	public Mask(String mask) {
		this.mask = mask;
		if (!verify(mask))
			throw new IllegalArgumentException("Mask doesn't match *!*@*");
	}

	public static boolean verify(String mask) {
		return mask.matches("[\\w\\*]+?![\\*\\w]+?@[\\*\\w\\.]+?");
	}

	public boolean matches(ServerUser user) {
		// do the nick
		String nickMask = mask.substring(0, mask.indexOf('!')).replace("*",
				".*");
		String userMask = mask.substring(mask.indexOf('!') + 1,
				mask.indexOf('@')).replace("*", ".*");
		String hostMask = mask.substring(mask.indexOf('@') + 1)
				.replace(".", "\\.").replace("*", ".*");
		return user.getNick().matches(nickMask)
				&& user.getUser().matches(userMask)
				&& user.getHost().matches(hostMask);
	}
}
