package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.Client;

public class CommandNotFoundHandler implements CommandHandler {
    @Override
    public void execute(Client client) {
        Messages.printMessage(Messages.COMMAND_NOT_FOUND);
    }
}
