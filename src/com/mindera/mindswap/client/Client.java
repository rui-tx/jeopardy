package com.mindera.mindswap.client;

import com.mindera.mindswap.Messages;
import com.mindera.mindswap.client.commands.Command;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private boolean locked;
    private long startTime;
    private long stopTime;
    private long messageTime;
    private ClientState state;
    private String[] lastCommand;

    /**
     * Main method to start the client application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start("localhost", 15000);
        } catch (IOException e) {
            Messages.printMessage(Messages.SERVER_OFFLINE);
            return;
        }
    }

    /**
     * Starts the client and connects to the specified server.
     *
     * @param host The server host name.
     * @param port The server port number.
     * @throws IOException If an I/O error occurs when creating the streams from the socket or when reading from the input stream.
     */
    private void start(String host, int port) throws IOException {
        socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        locked = false;
        state = ClientState.IDLE;
        lastCommand = new String[2];

        new Thread(new KeyboardHandler(out, socket)).start();
        String receivedMessage;
        while ((receivedMessage = in.readLine()) != null) {

            if (isCommand(receivedMessage)) {
                runCommand(receivedMessage);
                continue;
            }

            System.out.println(receivedMessage);
        }

        closeConnection();
        quit();
    }

    /**
     * Checks if message is a command
     *
     * @param message the message to check
     * @return true if message is a command, false otherwise
     */
    private boolean isCommand(String message) {
        return message.startsWith("/");
    }

    /**
     * Runs the command
     *
     * @param serverCommand the command to run received from the server
     */
    private void runCommand(String serverCommand) {
        // Split the command into the command and the arguments
        // Example: "/help" -> "help", ""
        String description = serverCommand.split(" ")[0];
        Command command = Command.getCommandFromDescription(description);
        lastCommand[0] = description;

        if (serverCommand.split(" ").length == 2)
            lastCommand[1] = serverCommand.split(" ")[1];
        else lastCommand[1] = null;

        command.getHandler().execute(this);
    }

    /**
     * Closes the connection to the server.
     */
    public void closeConnection() {
        try {
            socket.close();
            Messages.printMessage(Messages.CONNECTION_CLOSED);
        } catch (IOException e) {
            Messages.printMessage(Messages.CONNECTION_CLOSED, "but an error occurred: " + e.getMessage());
            quit();
        } catch (NullPointerException e) {
            Messages.printMessage(Messages.CONNECTION_LOST, "Socket closed / Lost connection to server");
            quit();
        }
    }

    /**
     * Quit application.
     */
    public void quit() {
        System.exit(0);
    }

    /**
     * Set locked state of client
     *
     * @param locked true if client is locked, false otherwise
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Set start time of client input command
     *
     * @param startTime the start time of the client input command
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Set client state
     *
     * @param clientState
     */
    public void setState(ClientState clientState) {
        this.state = clientState;
    }

    public String[] getLastCommand() {
        return lastCommand;
    }

    /**
     * Handles keyboard input from the user and sends it to the server.
     */
    private class KeyboardHandler implements Runnable {
        private final BufferedWriter out;
        private final Socket socket;
        private final BufferedReader in;

        /**
         * Constructs a new KeyboardHandler.
         *
         * @param out The BufferedWriter to send messages to the server.
         * @param socket The client socket connected to the server.
         */
        public KeyboardHandler(BufferedWriter out, Socket socket) {
            this.out = out;
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(System.in));
        }

        /**
         * Runs the keyboard handler thread, reading input from the user and sending it to the server.
         */
        @Override
        public void run() {

            while (!socket.isClosed()) {
                try {

                    String input = in.readLine();


                    // just testing ascii art
                    if (input.equals(Command.TEST.getDescription())) {
                        runCommand(Command.TEST.getDescription());
                        continue;
                    }

                    // If the user types "/quit", quit the application
                    // TODO: Something feels wrong here...
                    if (input.equals(Command.QUIT.getDescription())) {
                        runCommand(Command.QUIT.getDescription());
                    }

                    // If the client is locked, ignore the input
                    if (locked) {
                        Messages.printMessage(Messages.LOCKED_OUT);
                        continue;
                    }

                    if (state == ClientState.QUESTIONING) {
                        String regex = "^(1[0-6]|[1-9])$";
                        if(!input.matches(regex)) {
                            Messages.printMessage(Messages.BAD_QUESTION);
                            Messages.printMessage(Messages.SELECT_QUESTION);
                            continue;
                        }
                    }

                    if (state == ClientState.ANSWERING) {
                        String regex = "^([1-4])$";
                        if(!input.matches(regex)) {
                            Messages.printMessage(Messages.BAD_ANSWER);
                            Messages.printMessage(Messages.SELECT_ANSWER);
                            continue;
                        }
                    }

                    // Ignore empty input
                    if (input.isEmpty()) {
                        continue;
                    }

                    // Calculate the time it took to respond to the message
                    stopTime = System.currentTimeMillis();
                    messageTime = stopTime - startTime;

                    send(input);

                } catch (IOException e) {
                    Messages.printMessage(Messages.CONNECTION_LOST);
                    closeConnection();
                }
            }
        }


        /**
         * Sends a message to the server after encoding it.
         *
         * @param message The message to send.
         */
        private void send(String message) {
            try {
                synchronized (out) {
                    out.write(encodeMessage(message));
                    out.newLine();
                    out.flush();
                }

            } catch (IOException e) {
                Messages.printMessage(Messages.CONNECTION_LOST, "Error sending message to server");
                closeConnection();
                quit();
            }
        }

        /**
         * Encodes the message by appending the message time.
         * In the future, other type of values maybe appended to the message.
         *
         * @param message The message to encode.
         * @return The encoded message with the message time appended.
         */
        private String encodeMessage(String message) {
            return message + ";" + messageTime;
        }
    }
}
