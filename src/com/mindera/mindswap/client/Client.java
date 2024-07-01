package com.mindera.mindswap.client;

import com.mindera.mindswap.Messages;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private boolean locked;
    private long startTime;
    private long stopTime;
    private long messageTime;

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
        Socket socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        locked = false;

        new Thread(new KeyboardHandler(out, socket)).start();
        String receivedMessage;
        while ((receivedMessage = in.readLine()) != null) {

            if (receivedMessage.equals("/lock")) {
                locked = true;
                Messages.printMessage(Messages.LOCKED_OUT);
                continue;
            }

            if (receivedMessage.equals("/unlock")) {
                locked = false;
                Messages.printMessage(Messages.UNLOCKED);
                startTime = System.currentTimeMillis();
                continue;
            }

            System.out.println(receivedMessage);
        }

        closeConnection();
        quit();
    }

    /**
     * Closes the connection to the server.
     */
    private void closeConnection() {
        try {
            socket.close();
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
    private void quit() {
        System.exit(0);
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

                    if (locked) {
                        Messages.printMessage(Messages.LOCKED_OUT);
                        continue;
                    }

                    stopTime = System.currentTimeMillis();
                    messageTime = stopTime - startTime;

                    send(input);

                    if (input.equals("/quit")) {
                        closeConnection();
                        quit();
                    }

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

