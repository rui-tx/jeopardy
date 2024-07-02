package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;

public class UnlockHandler implements CommandHandler{
    @Override
    public void execute(Client client) {
        client.setLocked(false);
        client.setStartTime(System.currentTimeMillis());
        Messages.printMessage(Messages.UNLOCKED);
    }
}