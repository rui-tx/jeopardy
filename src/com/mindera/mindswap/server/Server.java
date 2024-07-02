package com.mindera.mindswap.server;

import com.mindera.mindswap.Game;
import com.mindera.mindswap.board.Board;

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
    private boolean gameStarted;
    // Additions
    private Board board;
    private Game game;


    public Server(int port) {
        clients = new CopyOnWriteArrayList<>();
        this.port = port;
        this.gameStarted = false;
        // Additions
        board = new Board();
        game = new Game(board);
    }

    public void start() {
        this.start(this.port);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            threads = Executors.newCachedThreadPool();
            System.out.printf("Server started on port %d", port);

            // check if there are enough clients to start the game
            threads.submit(new Thread(() -> {
                while (clients.size() < MAX_CLIENTS) {
                    try {
                        Thread.sleep(10000);
                        System.out.println("Waiting for clients...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                broadcast("[server]", "Game will start in 10 seconds.");
                gameStart();
            }));

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

        ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket, "");
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

    public void gameStart() {
        clients.forEach(handler -> handler.send("Game started!"));
        this.gameStarted = true;

        String winner = "";
        while (true) {
            winner = gameTurn();
            broadcast("[server] Round winner: ", winner + " !");
        }
    }

    private String gameTurn() {
        String winner = "";
        long lowestTime = 1000000;

        for (ClientConnectionHandler handler : clients) {
            handler.send("/unlock");
            handler.send("It's your turn!");

            System.out.println("Answer from " + handler.getName() + ": " + answer);
            if (handler.getMessageTime() < lowestTime) {
                lowestTime = handler.getMessageTime();
                winner = handler.getName();
            }

            // Display the board and let the client select a question
            handler.send(String.valueOf(board.displayBoard()));
            handler.send("Select a question number (1-16):");
            int questionNumber = Integer.parseInt(handler.getAnswer());

            // Send the selected question to the client
            String questionResponse = board.selectQuestion(questionNumber);
            handler.send(questionResponse);

            // Receive the answer from the client
            handler.send("Select an answer (1-4):");
            int selectedAnswer = Integer.parseInt(handler.getAnswer());

            // Check the answer and send the result to the client
            String answerResponse = board.checkAnswer(questionNumber, selectedAnswer);
            handler.send(answerResponse);

            //String answer = handler.getAnswer();
            //System.out.println(answer);

            handler.send("/lock");
        }

        return winner;
    }

    public class ClientConnectionHandler implements Runnable {

        private final Socket clientSocket;
        private final BufferedWriter out;
        private final Scanner in;
        private String name;
        private String message;
        private long messageTime;
        private boolean gameTurn;

        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.clientSocket = clientSocket;
            this.name = name;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.in = new Scanner(clientSocket.getInputStream());
            this.gameTurn = false;
        }

        @Override
        public void run() {
            addClient(this);
            changeName();
            welcome();
            send("/lock");

            broadcast("[server]", this.getName() + " connected");
        }

        public synchronized String getAnswer() {
            String encodedMessage = "";
            String newMessage = "";
            String messageTime = "";

            try {
                System.out.println("Waiting for answer...");

                encodedMessage = in.nextLine();
                newMessage = encodedMessage.split(";")[0];
                messageTime = encodedMessage.split(";")[1];
                System.out.println("Answer received: " + newMessage + " at " + messageTime + "ms");

            } catch (NullPointerException e) {
                System.out.println(e.getMessage());
                removeClient(this);
            }

            if (encodedMessage == null) {
                System.out.println("Client disconnected");
                removeClient(this);
            }
            this.message = newMessage;
            this.messageTime = Long.parseLong(messageTime);
            return this.message;
        }

        public void changeName() {
            while (!name.matches("[a-zA-Z0-9]+")) {
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
                    "When enough players are connected, the game will start.";
            send(welcomeMessage);
        }

        public String getName() {
            return name;
        }

        public long getMessageTime() {
            return messageTime;
        }
    }
}
