package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;
import com.mindera.mindswap.client.ClientState;

public class LockHandler implements CommandHandler {
    @Override
    public void execute(Client client) {
        client.setLocked(true);
        client.setState(ClientState.IDLE);
        Messages.printMessage(Messages.LOCKED_OUT);
    }
}