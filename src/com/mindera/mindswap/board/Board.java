package com.mindera.mindswap.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Board {
    private static final int BOARD_SIZE = 4;
    private final Cell[][] gameBoard;
    private final Scanner scanner;

    public Board(Map<String, ArrayList<Question>> questionsByCategory, Map<String, ArrayList<Answer>> answersById) {
        gameBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        scanner = new Scanner(System.in);
        populateBoardWithQuestionsAndAnswers(questionsByCategory, answersById);
    }


    private void populateBoardWithQuestionsAndAnswers(Map<String, ArrayList<Question>> questionsByCategory, Map<String, ArrayList<Answer>> answersById) {
        List<String> categories = new ArrayList<>(questionsByCategory.keySet());

        IntStream.range(0, BOARD_SIZE).forEach(col -> {
                    if (col < categories.size()) {
                        String category = categories.get(col);
                        ArrayList<Question> questions = questionsByCategory.get(category);

                        IntStream.range(0, BOARD_SIZE).forEach(row -> {
                            if (row < questions.size()) {
                                Question question = questions.get(row);
                                ArrayList<Answer> answers = answersById.get(question.id);
                                gameBoard[row][col] = new Cell(question, answers);
                            } else {
                                gameBoard[row][col] = null;
                            }
                        });
                    }
                });
    }

    public void displayBoard() {
        int counter = 1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    System.out.println("Question " + counter++);
                }
                else {
                    System.out.println(" ");
                }
            }
            System.out.println();
        }
    }

    public void selectCell(int row, int col) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && gameBoard[row][col] != null) {
            Cell cell = gameBoard[row][col];
            System.out.println("Question: " + cell.question.questionText);

            int optionNumber = 1;
            for (Answer answer : cell.answers) {
                System.out.println(" - " + answer.answerText);
                optionNumber++;
            }

            System.out.print("Select an answer (1-4): ");
            int selectedAnswer = scanner.nextInt();
            checkAnswer(cell, selectedAnswer);

        } else {
            System.out.println("Invalid cell selected or no question in the selected cell.");
        }
    }

    private void checkAnswer(Cell cell, int selectedAnswer) {
        if (selectedAnswer > 0 && selectedAnswer <= cell.answers.size()) {
            Answer answer = cell.answers.get(selectedAnswer - 1);
            if (answer.isCorrect) {
                System.out.println("Correct answer!");
            } else {
                System.out.println("Incorrect answer.");
            }
        } else {
            System.out.println("Invalid answer selection.");
        }
    }
}
