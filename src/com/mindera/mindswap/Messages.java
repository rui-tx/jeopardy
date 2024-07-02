package com.mindera.mindswap;

public enum Messages {

    //Client messages
    CLIENT_CONNECTED("Client connected"),
    CLIENT_DISCONNECTED("Client disconnected"),
    CLIENT_MESSAGE("Client message"),
    LOCKED_OUT("You are locked out!"),
    UNLOCKED("You are now unlocked."),

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
