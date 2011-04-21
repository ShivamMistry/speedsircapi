package com.speed.irc.types;

import java.util.LinkedList;
import java.util.List;

public class Mode {
	private List<Character> modes = new LinkedList<Character>();

	public Mode(String modes) {
		if (!modes.isEmpty())
			parse(modes);
	}

	protected void clear() {
		modes.clear();
	}

	protected void parse(String modes) {
		int plus = modes.indexOf("+");
		int minus = modes.indexOf("-");
		if (plus != -1) {
			String s = null;
			if (minus != -1 && minus > plus) {
				s = modes.substring(plus + 1, minus);
			} else if (minus != -1 && minus < plus) {
				s = modes.substring(plus + 1);
			} else {
				s = modes.substring(1);
			}
			if (s != null)
				for (char c : s.toCharArray()) {
					this.modes.add(c);
				}
		}
		if (minus != -1) {
			String s = null;
			if (plus != -1 && plus > minus) {
				s = modes.substring(minus + 1, plus);
			} else if (minus != -1 && plus < minus) {
				s = modes.substring(minus + 1);
			} else {
				s = modes.substring(1);
			}
			if (s != null)
				for (char c : s.toCharArray()) {
					this.modes.remove(c);
				}

		}
	}
}
