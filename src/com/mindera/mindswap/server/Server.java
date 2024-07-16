package com.mindera.mindswap.server;

import com.mindera.mindswap.Constants;
import com.mindera.mindswap.Messages;
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

import static com.mindera.mindswap.Constants.BANNER;
import static com.mindera.mindswap.Constants.WELCOME_MESSAGE;
import static com.mindera.mindswap.Messages.SERVER_STARTED;
import static com.mindera.mindswap.utils.TerminalColors.*;


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
        this.port = port;
        gameStarted = false;
        currentPlayerIndex = 0;
        clients = new CopyOnWriteArrayList<>();
        board = new Board();
    }

    /**
     * Starts the server on the given port.
     */
    public void start() {
        this.start(this.port);
    }

    /**
     * Starts the server on the given port.
     * @param port The port to start the server on.
     */
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            threads = Executors.newCachedThreadPool();
            System.out.printf(SERVER_STARTED.toString(), port);

            // check if there are enough clients to start the game
            threads.submit(new Thread(() -> {
                while (clients.size() < MAX_CLIENTS) {
                    try {
                        Thread.sleep(10000);
                        Messages.printMessage(Messages.SERVER_WAITING_FOR_PLAYERS);
                    } catch (InterruptedException e) {
                        Messages.printMessage(Messages.ERROR, e.getMessage());
                        return;
                    }
                }
                gameStart();
            }));

            while (true) {
                acceptConnection();
            }
        } catch (IOException e) {
            Messages.printMessage(Messages.SERVER_ERROR_CREATING);
        }
    }

    /**
     * Accepts a new connection from a client.
     * @throws IOException  If an I/O error occurs.
     */
    public void acceptConnection() throws IOException {
        Socket clientSocket = serverSocket.accept();
        if (clients.size() + 1 > MAX_CLIENTS) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            out.write(Messages.SERVER_FULL.toString());
            out.newLine();
            out.flush();
            out.close();
            clientSocket.close();
            return;
        }
        ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket, "");
        threads.submit(clientConnectionHandler);
    }

    /**
     * Adds a client to the list of clients.
     * @param cHandler The client to add.
     */
    private void addClient(ClientConnectionHandler cHandler) {
        clients.add(cHandler);
    }

    /**
     * Broadcasts a message to all clients.
     * @param message The message to broadcast.
     */
    public void broadcast(String message) {
        clients.forEach(handler -> handler.send(message));
    }

    /**
     * Starts the game.
     * This method will wait for all clients to be ready before starting the game.
     */
    public void gameStart() {

        while (!clients.stream().allMatch(ClientConnectionHandler::isReady)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Messages.printMessage(Messages.ERROR, e.getMessage());
                return;
            }
        }

        clients.forEach(handler -> handler.send(Messages.GAME_STARTED.toString()));
        gameStarted = true;

        String winner = "";
        while (!board.isGameOver()) {
            winner = gameTurn();
            broadcast(ANSI_PURPLE + "=== Round Winner === -> " + ANSI_RESET + ANSI_GREEN + winner + ANSI_RESET);
            sendScoreboard();
        }
        // Final broadcast of scores when the game is over
        finalScoreboard();
        clients.forEach(ClientConnectionHandler::close);
        clients.clear();
    }

    /**
     * Finalizes the game by broadcasting the final scoreboard and sending the winner's name and score.
     */
    private void finalScoreboard() {
        broadcast(ANSI_CYAN + " FINAL SCORES \n" + ANSI_RESET);
        sendScoreboard();
        ClientConnectionHandler winner = clients.stream()
                .max(Comparator.comparing(ClientConnectionHandler::getScore)).get();

        broadcast("Winner: " + ANSI_GREEN + winner.getName() + ANSI_RESET +
                " with " + ANSI_PURPLE + winner.getScore()  + "$" + ANSI_RESET);

        winner.send(Constants.WON);
        winner.send("/sound win");

        clients.forEach(handler -> {
            if (!handler.getName().equals(winner.getName())) {
                handler.send(Constants.LOST);
                handler.send("/sound lost");
            }
        });
    }

    /**
     * Sends the scoreboard to all clients.
     */
    private void sendScoreboard() {
        StringBuilder prettyScoreboard = new StringBuilder();
        prettyScoreboard.append(ANSI_CYAN + " =========== SCOREBOARD ========== \n" + ANSI_RESET);
        String fmt = ANSI_WHITE + "| %-18s | %-2s | %-4s | " + ANSI_RESET + "\n";
        prettyScoreboard.append(String.format(fmt, "Player", "W", "Score"));
        prettyScoreboard.append(ANSI_CYAN + " ================================= \n" + ANSI_RESET);
        clients.forEach(handler -> {
            String format = ANSI_PURPLE + "| %-18s | %-2s | %-4s  | " + ANSI_RESET + "\n";
            prettyScoreboard.append(String.format(format, handler.getName(), handler.getTurnsWon(), handler.getScore()));
        });
        prettyScoreboard.append(ANSI_CYAN + " ================================= \n" + ANSI_RESET);
        broadcast(prettyScoreboard.toString());
    }


    private Map<ClientConnectionHandler, Integer> collectAnswers() {
        Map<ClientConnectionHandler, Integer> playerAnswers = new HashMap<>();
        List<Thread> countdownThreads = new ArrayList<>();

        for (ClientConnectionHandler handler : clients) {
            Thread countdownThread = new Thread(() -> {
                handler.send("/sound cue1");

                // Countdown and input handling
                final int countdownTime = 10; // seconds
                final boolean[] inputReceived = {false};
                final String[] input = {null};

                Thread inputThread = new Thread(() -> {
                    input[0] = handler.getAnswer();
                    synchronized (inputReceived) {
                        inputReceived[0] = true;
                        inputReceived.notify();
                    }
                });

                inputThread.start();

                for (int i = countdownTime; i >= 0; i--) {
                    try {

                        // Send countdown message
                        handler.send("Time remaining: " + i + " seconds");

                        // Check if input is received
                        synchronized (inputReceived) {
                            if (inputReceived[0]) {
                                break;
                            }
                        }

                        // Wait for 1 second
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Lock input after countdown
                handler.send("Time remaining: 0 seconds");
                handler.send("/lock");

                // Interrupt input thread if time is up and input not received
                if (!inputReceived[0]) {
                    inputThread.interrupt();
                    input[0] = "-1"; // Mark as wrong answer if not responded in time
                }

                if (input[0] == null) {
                    return;
                }

                String cleanedInput = input[0].replaceAll("\\s", ""); // Remove all white spaces
                int selectedAnswer = Integer.parseInt(cleanedInput);

                synchronized (playerAnswers) {
                    playerAnswers.put(handler, selectedAnswer);
                }
            });

            countdownThreads.add(countdownThread);
            countdownThread.start();
        }

        for (Thread thread : countdownThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Messages.printMessage(Messages.ERROR, e.getMessage());
                return null;
            }
        }

        return playerAnswers;
    }

    /**
     * Broadcasts the question and answers to all clients.
     * @param questionNumber The number of the question to broadcast.
     * @param currentHandler The current client to broadcast the question and answers.
     */
    private void broadcastQuestionAndAnswers(int questionNumber, ClientConnectionHandler currentHandler) {
        // Broadcast the selected question to all players
        String questionResponse = board.selectQuestion(questionNumber);
        broadcast("\nQuestion selected by " + ANSI_GREEN + currentHandler.getName() + ANSI_RESET);
        broadcast("Value: " + questionResponse + "\n");

        // Notify all players to select an answer
        broadcast("/unlock");
        broadcast("Select an answer (1-4):");
        broadcast("/state answer");
    }

    /**
     * Handles the question selection for the current player.
     * @param currentHandler The current client to handle the question selection.
     * @return The number of the selected question.
     */
    private int handleQuestionSelection(ClientConnectionHandler currentHandler) {
        broadcast(ANSI_WHITE + currentHandler.getName() + " is selecting a question, please wait..." + ANSI_RESET);
        currentHandler.send("/unlock");
        currentHandler.send(ANSI_GREEN + "It's your turn!" + ANSI_RESET);
        currentHandler.send("/sound turn");

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

    /**
     * Selects the next player to play.
     * @return The next player to play.
     * If the current player is the last player, the first player will be returned.
     */
    private ClientConnectionHandler selectPlayer() {
        ClientConnectionHandler currentPlayer = clients.get(currentPlayerIndex);
        currentPlayerIndex = (currentPlayerIndex + 1) % clients.size();
        return currentPlayer;
    }

    /**
     * Handles the game turn for the current player.
     * @return The name of the winner.
     * If no winner is found, the method will return "No winner in this round".
     * If the game is over, the method will return the name of the winner.
     */
    private String gameTurn() {
        String winner = "No winner in this round";
        long lowestTime = 1_000_000_000;
        Set<ClientConnectionHandler> winners = new HashSet<>();

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

            // Get the player with the lowest response time
            if (handler.getMessageTime() < lowestTime) {
                lowestTime = handler.getMessageTime();
            }

            // Check if a player guessed correctly
            if (board.checkAnswerBool(questionNumber, selectedAnswer)) {
                winners.add(handler);
            }
        }

        Optional<ClientConnectionHandler> roundWinner = winners.stream()
                .min((a, b) -> Math.toIntExact(a.getMessageTime() - b.getMessageTime()));

        if (roundWinner.isPresent()) {
            roundWinner.get().turnWon();
            roundWinner.get().increaseScore(board.getQuestionValue(questionNumber));
            winner = roundWinner.get().getName();
        }

        // Lock all players again
        broadcast("/lock");

        // Change question isAnswered to true
        board.processQuestionBoolean(questionNumber, true);

        return winner;
    }


    /**
     * Handles a client connection.
     */
    public class ClientConnectionHandler implements Runnable {

        private final Socket clientSocket;
        private final BufferedWriter out;
        private final Scanner in;
        private String name;
        private String message;
        private long messageTime;
        private boolean gameTurn;
        private boolean hasPlayed;
        private int turnsWon;
        private int score;
        private boolean isReady;

        /**
         * Creates a new client connection handler.
         * @param clientSocket The client socket.
         * @param name The name of the client.
         * @throws IOException If an I/O error occurs.
         */
        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.clientSocket = clientSocket;
            this.name = name;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.in = new Scanner(clientSocket.getInputStream());
            this.gameTurn = false;
            this.hasPlayed = false;
            this.isReady = false;
        }

        /**
         * Runs the client connection handler.
         */
        @Override
        public void run() {
            addClient(this);
            changeName();
            welcome();
            send("/lock");
            setReady(true);

            broadcast("[server] " + this.getName() + " connected");
        }

        /**
         * Gets the answer from the client.
         * @return The answer from the client.
         */
        public synchronized String getAnswer() {
            String encodedMessage = "";
            String newMessage = "";
            String messageTime = "";

            try {
                Messages.printMessage(Messages.SERVER_WAITING_FOR_MESSAGE);

                encodedMessage = in.nextLine();
                newMessage = encodedMessage.split(";")[0];
                messageTime = encodedMessage.split(";")[1];
                System.out.println("Answer received: " + newMessage + " at " + messageTime + "ms");

            } catch (NullPointerException e) {
                System.out.println(Messages.ERROR + ": " + e.getMessage());
                removeClient(this);
            } catch (Exception e) {
                System.out.println(Messages.ERROR + ": " + e.getMessage());
                removeClient(this);
                return null;
            }

            if (encodedMessage == null) {
                Messages.printMessage(Messages.CLIENT_DISCONNECTED);
                removeClient(this);
            }
            this.message = newMessage;
            this.messageTime = Long.parseLong(messageTime);
            return this.message;
        }

        /**
         * Changes the name of the client.
         */
        public void changeName() {
            while (!name.matches("[a-zA-Z0-9]+")) {
                try {
                    out.write("Please enter a your name:");
                    out.newLine();
                    out.flush();
                    name = getAnswer();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Removes a client from the list of clients.
         * @param cHandler The client to remove.
         */
        public void removeClient(ClientConnectionHandler cHandler) {
            clients.remove(cHandler);
            cHandler.close();
        }

        /**
         * Sends a message to the client.
         * @param message The message to send.
         */
        public void send(String message) {
            try {
                synchronized (out) {
                    out.write(message);
                    out.newLine();
                    out.flush();
                }

            } catch (IOException e) {
                Messages.printMessage(Messages.SERVER_ERROR_SENDING_MESSAGE);
                removeClient(this);
            }
        }

        /**
         * Closes the client connection.
         */
        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Messages.printMessage(Messages.SERVER_ERROR_CLOSING_CLIENT);
            }
        }

        /**
         * Sends the welcome message and banner to the client.
         */
        private void welcome() {
            send(WELCOME_MESSAGE);
            send(BANNER);
            send("/sound intro");
        }

        /**
         * Gets the name of the client.
         * @return The name of the client.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the message time of the last message received from the client.
         * @return The message time of the last message received from the client.
         */
        public long getMessageTime() {
            return messageTime;
        }

        /**
         * Gets the number of turns won by the client.
         * @return The number of turns won by the client.
         */
        public int getTurnsWon() {
            return turnsWon;
        }

        /**
         * Increases the number of turns won by the client by 1.
         */
        public void turnWon() {
            turnsWon++;
        }

        /**
         * Gets the score of the client.
         * @return The score of the client.
         */
        public int getScore() {
            return score;
        }

        /**
         * Increases the score of the client by the given points.
         * @param points The points to increase the score by.
         */
        public void increaseScore(int points) {
            score += points;
        }

        /**
         * Checks if the client is ready.
         * @return True if the client is ready, false otherwise.
         */
        public boolean isReady() {
            return isReady;
        }

        /**
         * Sets the ready status of the client.
         * @param ready The ready status of the client.
         */
        public void setReady(boolean ready) {
            isReady = ready;
        }
    }
}
