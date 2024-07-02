package com.mindera.mindswap.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private boolean locked = false;

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start("localhost", 15000);
        } catch (IOException e) {
            System.out.println("Connection closed...");
        }
    }

    private void start(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        new Thread(new KeyboardHandler(out, socket)).start();
        String line;
        while ((line = in.readLine()) != null) {

            if (line.equals("/lock")) {
                locked = true;
                System.out.println("You are locked out!");
                continue;
            }

            if (line.equals("/unlock")) {
                locked = false;
                System.out.println("You are now unlocked.");
                continue;
            }

            System.out.println(line);
        }
        socket.close();
    }

    private class KeyboardHandler implements Runnable {
        private final BufferedWriter out;
        private final Socket socket;
        private final BufferedReader in;

        public KeyboardHandler(BufferedWriter out, Socket socket) {
            this.out = out;
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(System.in));
        }

        @Override
        public void run() {

            while (!socket.isClosed()) {
                try {
                    String line = in.readLine();

                    if (locked) {
                        System.out.println("You are locked out!");
                        continue;
                    }

                    out.write(line);
                    out.newLine();
                    out.flush();

                    if (line.equals("/quit")) {
                        socket.close();
                        System.exit(0);
                    }

                } catch (IOException e) {
                    System.out.println("Server connection lost...");
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
