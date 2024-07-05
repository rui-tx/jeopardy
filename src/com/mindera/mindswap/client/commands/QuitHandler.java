package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

/**
 * Handler for the quit command.
 */
public class QuitHandler implements CommandHandler {

    /**
     * Executes the quit command by closing the connection and quitting the client.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        client.closeConnection();
        client.quit();
    }
}
