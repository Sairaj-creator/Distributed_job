package com.taskflow.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {
    @Test
    void parsesCommandAndArguments() {
        CommandParser.ParsedCommand command = new CommandParser().parse(new String[]{"history", "job", "--limit", "3"});

        assertEquals("history", command.name());
        assertEquals(3, command.arguments().size());
        assertEquals("job", command.arguments().get(0));
    }

    @Test
    void emptyArgsBecomeHelp() {
        assertEquals("help", new CommandParser().parse(new String[0]).name());
    }
}
