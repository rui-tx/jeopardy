package com.mindera.mindswap.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

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

        // check for unused connections
        Thread checkServerConnection = new Thread(() -> {
            try {
                while (true) {
                    if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
                        System.out.println("Connection to the server lost.");
                        this.socket.close();
                        return;
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }

        });
        checkServerConnection.start();

        // read thread
        Thread read = new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    // current read stream from the server ie stuff that the server sends will be here
                    in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                    Iterator<String> it = in.lines().iterator();
                    while (it.hasNext()) {
                        String message = it.next();
                        if (message == null) {
                            this.close();
                            return;
                        }

                        System.out.printf("%s\n", message);
                    }

                } catch (IOException e) {
                    System.out.println("No connection to the server.");
                    break;
                    //throw new RuntimeException(e);
                }
            }

            try {
                this.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // write thread
        Thread write = new Thread(() -> {
            String command;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (!socket.isClosed()) {
                try {
                    // current write stream to the server ie stuff we write here will go to the server
                    out = new PrintWriter(this.socket.getOutputStream(), true);
                    command = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (command.equals("/exit")) {
                    try {
                        System.out.println("Goodbye!");
                        out.println("/exit");
                        this.close();
                        return;

                    } catch (IOException e) {
                        System.out.println("No connection to the server.");
                        try {
                            this.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        break;
                    }
                }

                out.println(command); //send to server the command
                out.flush();
            }
            
            try {
                this.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        read.start();
        write.start();
    }
}