package com.nisovin.magicspells.util.config.dialog;

import java.util.function.Consumer;

public final class Debugger {

	private final Consumer<String> config;
	private final Consumer<String> cast;

	public Debugger(Consumer<String> config, Consumer<String> cast) {
		this.config = config;
		this.cast = cast;
	}

	public void config(String text) {
		config.accept(text);
	}

	public void cast(String text) {
		cast.accept(text);
	}

}
