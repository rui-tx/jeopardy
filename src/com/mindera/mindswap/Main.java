package com.mindera.mindswap;

import com.mindera.mindswap.server.Server;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}