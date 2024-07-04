package com.mindera.mindswap.client.commands;

public enum Command {
    LOCK("/lock", new LockHandler()),
    UNLOCK("/unlock", new UnlockHandler()),
    QUIT("/quit", new QuitHandler()),
    TEST("/test", new TestHandler()),
    STATE("/state", new StateHandler()),
    SOUND("/sound", new SoundHandler()),

    NOT_FOUND("Command not found", new CommandNotFoundHandler());

    private final String description;
    private final CommandHandler handler;

    /**
     * Constructs a new Command with the specified description and handler.
     *
     * @param description the description of the command
     * @param handler     the handler associated with the command
     */
    Command(String description, CommandHandler handler) {
        this.description = description;
        this.handler = handler;
    }

    /**
     * Retrieves the Command corresponding to the given description.
     *
     * @param description the description of the command
     * @return the Command matching the description, or NOT_FOUND if no match is found
     */
    public static Command getCommandFromDescription(String description) {
        for (Command command : values()) {
            if (description.equals(command.description)) {
                return command;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Gets the handler associated with the command.
     *
     * @return the handler for the command
     */
    public CommandHandler getHandler() {
        return handler;
    }

    /**
     * Gets the description of the command.
     *
     * @return the description of the command
     */
    public String getDescription() {
        return description;
    }
}