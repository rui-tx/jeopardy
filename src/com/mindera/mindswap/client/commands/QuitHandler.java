package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

public class QuitHandler implements CommandHandler {
    @Override
    public void execute(Client client) {
        client.closeConnection();
        client.quit();
    }
}
