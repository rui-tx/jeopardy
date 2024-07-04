package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;

/**
 * Handler for the unlock command.
 */
public class UnlockHandler implements CommandHandler {

    /**
     * Executes the unlock command by setting the client's locked flag to false and setting the start time.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        client.setLocked(false);
        client.setStartTime(System.currentTimeMillis());
        Messages.printMessage(Messages.UNLOCKED);
    }
}