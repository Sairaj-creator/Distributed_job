package com.taskflow.cli;

import java.util.Arrays;
import java.util.List;

/**
 * Minimal command parser for TaskFlow CLI commands.
 */
public final class CommandParser {
    public ParsedCommand parse(String[] args) {
        if (args == null || args.length == 0) {
            return new ParsedCommand("help", List.of());
        }
        return new ParsedCommand(args[0], Arrays.stream(args).skip(1).toList());
    }

    public record ParsedCommand(String name, List<String> arguments) {
    }
}
