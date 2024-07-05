package com.mindera.mindswap;

import com.mindera.mindswap.server.Server;


public class Main {
    public static void main(String[] args) {

        Server server = new Server(15000);
        server.start();
    }
}