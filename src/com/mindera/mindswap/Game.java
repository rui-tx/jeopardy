package com.mindera.mindswap;

import com.mindera.mindswap.board.Board;

import java.util.Scanner;


public class Game {
    private final Board board;
    private final Scanner scanner;

    public Game(Board board) {
        this.board = board;
        scanner = new Scanner(System.in);
    }



    public void start() {
        //board.displayBoard();

        while (true) {

            if (scanner.hasNextInt()) {
                int questionNumber = scanner.nextInt();
                getQuestion(questionNumber);
            }
        }
    }

    private void getQuestion(int questionNumber) {
        System.out.print("Enter question number (1-16): ");

        if (scanner.hasNextInt()) {
            // int questionNumber = scanner.nextInt();
            board.selectQuestion(questionNumber);
            }
        }

}
