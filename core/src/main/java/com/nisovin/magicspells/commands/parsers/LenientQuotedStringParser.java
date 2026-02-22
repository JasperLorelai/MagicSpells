package com.nisovin.magicspells.commands.parsers;

import org.jetbrains.annotations.NotNull;

import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.standard.StringParser;

public class LenientQuotedStringParser<C> implements ArgumentParser<C, String> {

	public static <C> ParserDescriptor<C, String> lenientQuotedStringParser() {
		return ParserDescriptor.of(new LenientQuotedStringParser<>(), String.class);
	}

	private final StringParser<C> stringParser = new StringParser<>(StringParser.StringMode.QUOTED);

	@Override
	public @NotNull ArgumentParseResult<String> parse(@NotNull CommandContext<C> commandContext, @NotNull CommandInput commandInput) {
		return stringParser.parse(commandContext, commandInput);
	}

}
