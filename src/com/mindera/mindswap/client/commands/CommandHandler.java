package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

/**
 * Functional interface representing a handler for commands.
 */
@FunctionalInterface
public interface CommandHandler {

    /**
     * Executes the command using the provided client.
     *
     * @param client the client on which the command will be executed
     */
    void execute(Client client);
}