package com.speed.irc.types;

public interface Filter {
	public boolean accept(String s);

	public void process(String s);
}
