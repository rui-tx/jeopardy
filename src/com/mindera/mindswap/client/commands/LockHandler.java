package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;
import com.mindera.mindswap.client.ClientState;

/**
 * Handler for the lock command.
 */
public class LockHandler implements CommandHandler {

    /**
     * Executes the lock command by setting the client's state to IDLE and setting the locked flag to true.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        client.setLocked(true);
        client.setState(ClientState.IDLE);
        //Messages.printMessage(Messages.LOCKED_OUT);
    }
}