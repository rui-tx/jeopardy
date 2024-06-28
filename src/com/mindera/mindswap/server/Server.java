package com.mindera.mindswap.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int MAX_CLIENTS = 2;

    private final List<ClientConnectionHandler> clients;
    private ServerSocket serverSocket;
    private int port;
    private ExecutorService threads;


    public Server(int port) {
        clients = new CopyOnWriteArrayList<>();
        this.port = port;
    }

    public void start() {
        this.start(this.port);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            threads = Executors.newCachedThreadPool();
            System.out.printf("Server started on port %d", port);
            while (true) {
                acceptConnection();
            }
        } catch (IOException e) {
            System.out.println("Error starting server");
        }

    }

    public void acceptConnection() throws IOException {
        Socket clientSocket = serverSocket.accept();
        if (clients.size() + 1 > MAX_CLIENTS) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String message = "Server is full, please try again later.";
            out.write(message);
            out.newLine();
            out.flush();
            out.close();
            clientSocket.close();
            return;
        }

        ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket, "client" + clients.size());
        threads.submit(clientConnectionHandler);
    }

    private void addClient(ClientConnectionHandler cHandler) {
        clients.add(cHandler);
        //cHandler.send(cHandler.getName());
    }

    public void broadcast(String name, String message) {
        clients.stream()
                .filter(handler -> !handler.getName().equals(name))
                .forEach(handler -> handler.send(name + ": " + message));
    }

    public class ClientConnectionHandler implements Runnable {

        private final Socket clientSocket;
        private final BufferedWriter out;
        private final Scanner in;
        private String name;
        private String message;

        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.clientSocket = clientSocket;
            this.name = name;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.in = new Scanner(clientSocket.getInputStream());
        }

        @Override
        public void run() {
            addClient(this);
            changeName();
            welcome();

            broadcast("[server]", this.getName() + " connected");

            while (in.hasNext()) {
                getAnswer();

                if (message.isEmpty()) {
                    continue;
                }

                broadcast(name, message);
            }
        }

        public String getAnswer() {
            try {
                message = in.nextLine();
            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                removeClient(this);
            } finally {
                if (message == null) {
                    System.out.println("Client disconnected");
                    removeClient(this);
                }
            }

            return this.message;
        }

        public void changeName() {
            while (!name.matches("[a-zA-Z]+")) {
                try {
                    out.write("Please enter a your name:");
                    out.newLine();
                    out.flush();
                    name = getAnswer();

                    /*
                    boolean isNameTaken = clients.stream()
                            .anyMatch(client -> client.getName().equals(newName));
                    if (isNameTaken) {
                        out.write("Name already taken. Please enter a different name");
                        out.flush();
                    }
                    nameValid = true;
                     */

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }


        }

        public void removeClient(ClientConnectionHandler cHandler) {
            clients.remove(cHandler);
            cHandler.close();
        }

        public void send(String message) {
            try {
                synchronized (out) {
                    out.write(message);
                    out.newLine();
                    out.flush();
                }

            } catch (IOException e) {
                System.out.println("Error sending message to client, removing it...");
                removeClient(this);
            }
        }

        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client");
            }
        }

        private void welcome() {
            String welcomeMessage = "Welcome to the Jeopardy server!\n" +
                    "You are on the chat lobby. When enough players are connected, the game will start.";
            send(welcomeMessage);
        }

        public String getName() {
            return name;
        }
    }
}
