package com.mindera.mindswap.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientLauncher {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 15000;
        Client client = new Client();
        Thread clientThread = null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        while (true) {
            System.out.print("[command]> ");
            try {
                command = reader.readLine();

                if (command.equals("/connect")) {
                    Socket clientSocket = client.init(host, port); // blocking method
                    if (clientSocket == null) {
                        continue;
                    }

                    if (!clientSocket.isConnected()) {
                        System.out.println("client already connected");
                        continue;
                    }

                    // TODO: add client pool thread with executor
                    clientThread = new Thread(client);
                    clientThread.start();

                    break;
                }

                if (command.equals("/exit")) {

                    try {
                        clientThread.join();
                        System.exit(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

}
