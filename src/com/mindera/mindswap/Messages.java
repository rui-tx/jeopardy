package com.mindera.mindswap;

import static com.mindera.mindswap.utils.TerminalColors.ANSI_RESET;
import static com.mindera.mindswap.utils.TerminalColors.ANSI_YELLOW;

public enum Messages {

    //Client messages
    CLIENT_CONNECTED("Client connected"),
    CLIENT_DISCONNECTED("Client disconnected"),
    CLIENT_MESSAGE("Client message"),
    LOCKED_OUT("You are locked out!"),
    UNLOCKED("You are now unlocked."),
    BAD_QUESTION(ANSI_YELLOW + "That is not a valid question number." + ANSI_RESET + System.lineSeparator()),
    BAD_ANSWER(ANSI_YELLOW + "That is not a valid answer number." + ANSI_RESET + System.lineSeparator()),
    SELECT_QUESTION("Select a question (1-16):"),
    SELECT_ANSWER("Select an answer (1-4):"),

    //Server messages
    SERVER_CONNECTED("Server connected"),
    SERVER_DISCONNECTED("Server disconnected"),
    SERVER_OFFLINE("Server offline"),
    SERVER_MESSAGE("Server message"),

    //Connection messages
    CONNECTION_ACCEPTED("Connection accepted"),
    CONNECTION_REFUSED("Connection refused"),
    CONNECTION_CLOSED("Connection closed"),
    CONNECTION_LOST("Connection lost"),

    //Error messages
    ERROR("Something went wrong"),
    COMMAND_NOT_FOUND("Command not found");


    private final String message;

    Messages(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    public static void printMessage(Messages message) {
        System.out.println(message.toString());
    }

    public static void printMessage(Messages message, String description) {
        System.out.println(message.toString() + ": " + description);
    }
}
