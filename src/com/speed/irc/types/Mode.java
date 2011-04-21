package com.speed.irc.types;

import java.util.LinkedList;
import java.util.List;

public class Mode {
	private List<Character> modes = new LinkedList<Character>();
	public static char[] letters;
	public static char[] symbols;

	public Mode(String modes) {
		if (!modes.isEmpty())
			parse(modes);
	}

	protected void clear() {
		modes.clear();
	}

	public static char letterToSymbol(char letter) {
		for (int i = 0; i < letters.length; i++) {
			if (letters[i] == letter) {
				return symbols[i];
			}
		}
		return '0';
	}

	public static char symbolToLetter(char symbol) {
		for (int i = 0; i < symbols.length; i++) {
			if (symbols[i] == symbol) {
				return letters[i];
			}
		}
		return '0';
	}

	protected void parse(String modes) {
		boolean plus = false;
		int index = 0;
		for (int i = 0; i < modes.toCharArray().length; i++) {
			char c = modes.toCharArray()[i];
			if (c == '+') {
				plus = true;
				continue;
			} else if (c == '-') {
				plus = false;
				continue;
			}
			index++;
			if (plus) {
				this.modes.add(c);
			} else {
				this.modes.remove(c);
			}
		}
	}
}
