package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;

/**
 * Handler for the case when a command is not found.
 */
public class CommandNotFoundHandler implements CommandHandler {

    /**
     * Executes the command not found action by printing a command not found message.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        Messages.printMessage(Messages.COMMAND_NOT_FOUND);
    }
}
