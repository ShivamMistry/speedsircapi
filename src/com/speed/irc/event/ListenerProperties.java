package com.speed.irc.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ListenerProperties {
	Class<? extends IRCEvent>[] events();
}
