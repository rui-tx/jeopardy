package com.mindera.mindswap.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final String PRETTY_NAME = "Jeopardy Server";
    private final String HOST = "localhost";
    private final int PORT = 15000;

    private final List<ClientHandler> connectionList = new LinkedList<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private ServerSocket socket;


    public Server() {
        //test
        //this is a test
    }

    public void start() {

        ServerSocket server = this.init();

        while (true) {
            Socket clientSocket;
            synchronized (server) {
                clientSocket = this.initClientConnection(server); // blocking method
            }

            ClientHandler conn = new ClientHandler(this, clientSocket);
            Thread thread = new Thread(conn, clientSocket.getPort() + "");

            threadPool.submit(thread);
            connectionList.add(conn);
            this.sendMessageToClients("[server]> [" + clientSocket.getPort() + "] joined the server");
        }

    }

    private ServerSocket init() {

        try {
            this.socket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("[server]> socket created at port %d\n", PORT);
        return this.socket;
    }

    private Socket initClientConnection(ServerSocket serverSocket) {
        Socket socket;

        try {
            socket = serverSocket.accept();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        return socket;
    }


    public static class ClientHandler implements Runnable {

        private final Server server;
        private Socket socket;
        private String clientName;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Server server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }


        @Override
        public void run() {

        }
    }
}
