package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;

public class LockHandler implements CommandHandler {
    @Override
    public void execute(Client client) {
        client.setLocked(true);
        Messages.printMessage(Messages.LOCKED_OUT);
    }
}