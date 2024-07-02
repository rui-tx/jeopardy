package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;

@FunctionalInterface
public interface CommandHandler {
    void execute(Client client);
}