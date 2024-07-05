package com.mindera.mindswap.client.commands;

import com.mindera.mindswap.client.Client;
import com.mindera.mindswap.client.ClientState;

/**
 * Handler for the state command.
 */
public class StateHandler implements CommandHandler {

    /**
     * Executes the state command by setting the client's state to the specified state.
     *
     * @param client the client on which the command will be executed
     */
    @Override
    public void execute(Client client) {
        if (client.getLastCommand() != null && client.getLastCommand()[0].equals("/state")) {
            switch (client.getLastCommand()[1]) {
                case "question":
                    client.setState(ClientState.QUESTIONING);
                    break;
                case "answer":
                    client.setState(ClientState.ANSWERING);
                    break;
                default:
                    client.setState(ClientState.IDLE);
            }
        }
    }
}
