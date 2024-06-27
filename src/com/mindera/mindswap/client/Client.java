package com.mindera.mindswap.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public Socket init(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            //System.out.println("Your ID is : [" + this.socket.getLocalPort() + "]");
            System.out.println("Connected to server.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return socket;
    }

    private void close() throws IOException {
        System.out.println("Connection to the server closed.");
        this.socket.close();
    }

    @Override
    public void run() {

        // read thread
        Thread read = new Thread(() -> {
            while (this.socket != null && !socket.isClosed()) {
                try {
                    // current read stream from the server ie stuff that the server sends will be here
                    in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    String message = in.readLine();
                    if (message == null) {
                        this.close();
                        return;
                    }
                    System.out.printf("%s\n", message);
                } catch (IOException e) {
                    System.out.println("No connection to the server.");
                    return;
                    //throw new RuntimeException(e);
                }
            }
        });

        // write thread
        Thread write = new Thread(() -> {
            String command;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (this.socket != null && !socket.isClosed()) {
                try {
                    // current write stream to the server ie stuff we write here will go to the server
                    out = new PrintWriter(this.socket.getOutputStream(), true);
                    command = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (command.equals("/exit")) {
                    try {
                        out.println("Goodbye!");
                        this.close();
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    //break;
                }

                out.println(command); //send to server the command
                out.flush();
            }
        });

        read.start();
        write.start();
    }
}