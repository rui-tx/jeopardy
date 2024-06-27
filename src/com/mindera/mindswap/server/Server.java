package com.mindera.mindswap.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int MAX_CLIENTS = 2;
    private final String PRETTY_NAME = "Jeopardy Server";
    private final String HOST = "localhost";
    private final int PORT = 15000;
    private final List<ClientHandler> connectionList = new CopyOnWriteArrayList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private ServerSocket socket;
    private boolean gameStarted = false;
    private boolean gameCanStart = false;

    public Server() {
    }

    public void start() {

        ServerSocket server = this.init();

        while (true) {
            Socket clientSocket;

            synchronized (server) {
                clientSocket = this.initClientConnection(server); // blocking method
            }

            if (clientSocket == null) {
                continue;
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

            if (connectionList.size() >= MAX_CLIENTS) {
                System.out.println("Server is full. Kicking client.");
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.println("Server is full. Please try again later.");
                out.flush();
                socket.close();
                return null;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        return socket;
    }

    private ServerSocket init() {

        try {
            this.socket = new ServerSocket(PORT);

            // check for enough players
            Thread waitForPlayers = new Thread(() -> {
                try {
                    while (true) {
                        if (connectionList.size() == MAX_CLIENTS) {
                            broadcast("Server", "Game is about to start. Please wait...");
                            gameStart();
                            Thread.currentThread().interrupt();
                        }
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
            waitForPlayers.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("[server]> Server created at port %d. Ready to accept connections.\n", PORT);

        return this.socket;
    }

    private void gameStart() {
        ///broadcast("Server", "Game is starting...");
        gameStarted = true;

        // lock all players
        for (ClientHandler currentPlayer : connectionList) {
            currentPlayer.locked = true;

        }

        // while game has not ended
        while (true) {
            // unlocks the 1st player
            //connectionList.getFirst().locked = false;

            Iterator<ClientHandler> it = connectionList.iterator();
            while (it.hasNext()) {
                ClientHandler currentPlayer = it.next();
                System.out.println(currentPlayer.clientName);
                currentPlayer.locked = false;

                broadcast("Server", currentPlayer.getName() + ", it's your turn!");
                // wait for a player to answer
                //String answer = this.waitForAnswer(currentPlayer);
                currentPlayer.waitForAnswer = true;

                //needs blocking method but this is not working
                String answer = this.waitForAnswer(currentPlayer);

                System.out.println("Server " + currentPlayer.getName() + " answered: " + currentPlayer.answer);
                currentPlayer.waitForAnswer = false;
                currentPlayer.locked = true;
            }

        }
    }

    private String waitForAnswer(ClientHandler client) {
        String answer;

        try {
            answer = client.in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return answer;
    }

    private void broadcast(String name, String message) {
        for (ClientHandler client : this.connectionList) {
            client.send(name + ": " + message);
        }
    }


    public class ClientHandler implements Runnable {

        private final PrintWriter out;
        private final BufferedReader in;
        private Socket socket;
        private String clientName;
        private String message;
        private boolean locked;
        private boolean waitForAnswer;
        private String answer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientName = "default";
            this.locked = false;
            this.waitForAnswer = false;
            this.answer = "";

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
            this.welcome();

            while (!socket.isClosed()) {
                try {
                    message = in.readLine();

                    if (message.isEmpty()) {
                        continue;
                    }

                    if (!gameStarted) {
                        this.broadcast(clientName, message);
                    }

                    if (this.locked) {
                        out.println("You are locked. Please wait for the current player to finish.");
                        out.flush();
                        continue;
                    }

                    if (gameStarted && waitForAnswer) {
                        this.answer = message;
                        continue;
                    }

                    if (message.equals("/exit")) {
                        closeConnection();
                        break;
                    }


                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        private void closeConnection() {
            try {
                this.socket.close();
                System.out.println("Client disconnected");
                connectionList.remove(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
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

        private void welcome() {
            synchronized (out) {
                out.println("Welcome to the Jeopardy server!");
                out.println("You are on the chat lobby. When enough players are connected, the game will start.");
                out.println("To chat with other players, just type and press <Enter>.");
                out.println("Type /help to see the list of commands.");
                out.println("Type /exit to exit.");
                out.flush();
            }
        }

        public void broadcast(String name, String message) {
            connectionList.stream()
                    .filter(client -> !client.getName().equals(name))
                    .forEach(client -> client.send(name + ": " + message));
        }

        public void sendToAll(String name, String message) {
            connectionList
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
