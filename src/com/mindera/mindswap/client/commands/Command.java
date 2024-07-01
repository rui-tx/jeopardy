package com.mindera.mindswap.client.commands;

public enum Command {
    LOCK("/lock", new LockHandler()),
    UNLOCK("/unlock", new UnlockHandler()),
    QUIT("/quit", new QuitHandler()),
    TEST("/test", new TestHandler()),

    NOT_FOUND("Command not found", new CommandNotFoundHandler());

    private final String description;
    private final CommandHandler handler;

    Command(String description, CommandHandler handler) {
        this.description = description;
        this.handler = handler;
    }

    public static Command getCommandFromDescription(String description) {
        for (Command command : values()) {
            if (description.equals(command.description)) {
                return command;
            }
        }
        return NOT_FOUND;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public String getDescription() {
        return description;
    }
}