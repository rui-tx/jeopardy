package com.mindera.mindswap.server;

import com.mindera.mindswap.board.Board;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
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
    private Board board;
    private int currentPlayerIndex;


    public Server(int port) {
        clients = new CopyOnWriteArrayList<>();
        this.port = port;
        this.gameStarted = false;
        board = new Board();
        currentPlayerIndex = 0;
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
                broadcast("[server] " + "Game will start in 10 seconds.");
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

    public void broadcast(String message) {
        clients.forEach(handler -> handler.send(message));
    }

    public void gameStart() {
        clients.forEach(handler -> handler.send("Game started!"));
        this.gameStarted = true;

        String winner = "";
        while (!board.isGameOver()) {
            winner = gameTurn();
            broadcast("[server] Round winner: " + winner + " !");
            broadcast("[server] Current score:");
            clients.forEach(handler -> broadcast(handler.getName() + " has " + handler.getTurnsWon() + " wins" +
                    " and " + handler.getScore() + "$"));
        }
        // Final broadcast of scores when the game is over
        broadcast("[server] Final scores:");
        clients.forEach(handler -> broadcast(handler.getName() + " has " + handler.getTurnsWon() + " wins" +
                " and " + handler.getScore() + "$"));
    }

    private Map<ClientConnectionHandler, Integer> collectAnswers() {
        Map<ClientConnectionHandler, Integer> playerAnswers = new HashMap<>();
        List<Thread> answerThreads = new ArrayList<>();

        for (ClientConnectionHandler handler : clients) {
            Thread answerThread = new Thread(() -> {
                String input = handler.getAnswer();
                handler.send("/lock");
                String cleanedInput = input.replaceAll("\\s", ""); // Remove all white spaces
                int selectedAnswer = Integer.parseInt(cleanedInput);
                synchronized (playerAnswers) {
                    playerAnswers.put(handler, selectedAnswer);
                }
            });
            answerThreads.add(answerThread);
            answerThread.start();
        }

        for (Thread thread : answerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return playerAnswers;
    }

    private void broadcastQuestionAndAnswers(int questionNumber, ClientConnectionHandler currentHandler) {
        // Broadcast the selected question to all players
        String questionResponse = board.selectQuestion(questionNumber);
        broadcast("Question selected by " + currentHandler.getName() + " for " + questionResponse);

        // Notify all players to select an answer
        broadcast("/unlock");
        broadcast("Select an answer (1-4):");
        broadcast("/state answer");
    }

    private int handleQuestionSelection(ClientConnectionHandler currentHandler) {
        currentHandler.send("/unlock");
        currentHandler.send("It's your turn!");

        // Display the board and let the current player select a question
        currentHandler.send(board.displayPrettyBoard());
        currentHandler.send("Select a question number (1-16):");
        currentHandler.send("/state question");

        int questionNumber;
        while (true) {
            String input = currentHandler.getAnswer();
            String cleanedInput = input.replaceAll("\\s", ""); // Remove all white spaces
            questionNumber = Integer.parseInt(cleanedInput);

            if (board.processQuestionBoolean(questionNumber, false)) {
                currentHandler.send("That question already been answered, choose another.");
            } else {
                break;
            }
        }
        currentHandler.send("/lock");

        return questionNumber;
    }

    private ClientConnectionHandler selectPlayer() {
        ClientConnectionHandler currentPlayer = clients.get(currentPlayerIndex);
        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        return currentPlayer;
    }

    private String gameTurn() {
        String winner = "No winner in this round";
        String fastestPlayer = "";
        long lowestTime = 1_000_000;

        // Select the current player to choose the question
        ClientConnectionHandler currentPlayer = selectPlayer();

        // Handle player question selection
        int questionNumber = handleQuestionSelection(currentPlayer);

        // Start a new thread to broadcast the question and answers
        threads.submit(new Thread(() -> broadcastQuestionAndAnswers(questionNumber, currentPlayer)));

        // Collect answers from all players
        Map<ClientConnectionHandler, Integer> playerAnswers = collectAnswers();

        // Check the answers and send the results to each player
        for (Map.Entry<ClientConnectionHandler, Integer> entry : playerAnswers.entrySet()) {
            ClientConnectionHandler handler = entry.getKey();
            int selectedAnswer = entry.getValue();
            String answerResponse = board.validateAnswer(questionNumber, selectedAnswer);
            handler.send(answerResponse);

            // Determine the winner based on the lowest response time
            if (handler.getMessageTime() < lowestTime) {
                lowestTime = handler.getMessageTime();
                fastestPlayer = handler.getName();
            }

            // check if the player won
            if (board.checkAnswerBool(questionNumber, selectedAnswer)) {
                if (handler.getName().equals(fastestPlayer)) {
                    handler.turnWon();
                    handler.increaseScore(board.getQuestionValue(questionNumber));
                    winner = handler.getName();
                }
            }
        }
        // Lock all players again
        broadcast("/lock");

        // Change question isAnswered to true
        board.processQuestionBoolean(questionNumber, true);

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
        private boolean hasPlayed;
        private int questionNumber;
        private int turnsWon;
        private int score;

        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.clientSocket = clientSocket;
            this.name = name;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.in = new Scanner(clientSocket.getInputStream());
            this.gameTurn = false;
            this.hasPlayed = false;
        }

        @Override
        public void run() {
            addClient(this);
            changeName();
            welcome();
            send("/lock");

            broadcast("[server] " + this.getName() + " connected");
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

        public int getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(int questionNumber) {
            this.questionNumber = questionNumber;
        }

        public int getTurnsWon() {
            return turnsWon;
        }

        public void turnWon() {
            turnsWon++;
        }

        public int getScore() {
            return score;
        }

        public void increaseScore(int points) {
            score += points;
        }
    }
}
