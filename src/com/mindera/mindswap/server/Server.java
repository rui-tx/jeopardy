package com.mindera.mindswap.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final String PRETTY_NAME = "Jeopardy Server";
    private final String HOST = "localhost";
    private final int PORT = 15000;

    private final List<ClientHandler> connectionList = new CopyOnWriteArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private ServerSocket socket;


    public Server() {
    }

    public void start() {

        ServerSocket server = this.init();

        while (true) {
            Socket clientSocket;

            synchronized (server) {
                clientSocket = this.initClientConnection(server); // blocking method
            }

            ClientHandler newClient = new ClientHandler(clientSocket);
            Thread thread = new Thread(newClient, clientSocket.getPort() + "");

            threadPool.submit(thread);
        }

    }

    private Socket initClientConnection(ServerSocket serverSocket) {
        Socket socket;

        try {
            socket = serverSocket.accept();
            System.out.println("Client connected");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        return socket;
    }

    private ServerSocket init() {

        try {
            this.socket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("[server]> Server created at port %d. Ready to accept connections.\n", PORT);
        return this.socket;
    }


    public class ClientHandler implements Runnable {

        private final PrintWriter out;
        private Socket socket;
        private String clientName;
        private BufferedReader in;
        private String message;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientName = "default";

            try {
                in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void run() {

            connectionList.add(this);
            this.changeName();
            
            while (!socket.isClosed()) {
                try {
                    message = in.readLine();

                    if (message.isEmpty()) {
                        out.println("Empty message");
                        out.flush();
                        continue;
                    }

                    this.broadcast(clientName, message);

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    //removeClient(this);
                }
            }
        }

        private void changeName() {
            boolean nameValid = false;
            while (!nameValid) {
                out.println("Please enter your name: ");
                out.flush();

                try {
                    String newClientName = in.readLine();

                    if (connectionList.isEmpty()) {
                        this.clientName = newClientName;
                        nameValid = true;
                        continue;
                    }

                    boolean isNameTaken = connectionList.stream()
                            .anyMatch(client -> client.getName().equals(newClientName));
                    if (isNameTaken) {
                        out.println("Name already taken. Please enter a different name");
                        out.flush();
                        continue;
                    }

                    this.clientName = newClientName;
                    nameValid = true;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }


        }

        public void broadcast(String name, String message) {
            connectionList.stream()
                    .filter(client -> !client.getName().equals(name))
                    .forEach(client -> client.send(name + ": " + message));
        }

        public void send(String message) {
            synchronized (out) {
                out.println(message);
                out.flush();
            }

        }

        public String getName() {
            return clientName;
        }
    }
}
