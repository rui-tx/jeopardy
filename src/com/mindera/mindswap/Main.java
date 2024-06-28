package com.mindera.mindswap;

import com.mindera.mindswap.board.Answer;
import com.mindera.mindswap.board.Board;
import com.mindera.mindswap.utils.CSVReader;
import com.mindera.mindswap.board.Question;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import static com.mindera.mindswap.Constants.ANSWERS_FILE_PATH;
import static com.mindera.mindswap.Constants.QUESTIONS_FILE_PATH;


public class Main {
    public static void main(String[] args) {

        Map<String, ArrayList<Question>> questionsByCategory = getQuestions();

        Map<String, ArrayList<Answer>> answersById = getAnswers();


        Board gameBoard = new Board(questionsByCategory, answersById);
        gameBoard.displayBoard();

        System.out.println();

        // Example of selecting a cell (1st row, 2nd column)
        //gameBoard.selectCell(0, 0);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter row (0-3): ");
            int row = scanner.nextInt();
            System.out.print("Enter col (0-3): ");
            int col = scanner.nextInt();

            gameBoard.selectCell(row, col);
        }

        /*
        gameBoard.selectCell(1, 0);
        gameBoard.selectCell(2, 0);
        gameBoard.selectCell(3, 0);
        gameBoard.selectCell(4, 0);

        gameBoard.selectCell(0, 1);
        gameBoard.selectCell(1, 1);
        gameBoard.selectCell(2, 1);
        gameBoard.selectCell(3, 1);
        gameBoard.selectCell(4, 1);

        gameBoard.selectCell(0, 2);
        gameBoard.selectCell(1, 2);
        gameBoard.selectCell(2, 2);
        gameBoard.selectCell(3, 2);
        gameBoard.selectCell(4, 2);

        gameBoard.selectCell(0, 3);
        gameBoard.selectCell(1, 3);
        gameBoard.selectCell(2, 3);
        gameBoard.selectCell(3, 3);
        gameBoard.selectCell(4, 3);

        gameBoard.selectCell(0, 4);

         */

    }

    private static Map<String, ArrayList<Answer>> getAnswers() {
        Map<String, ArrayList<Answer>> answersById = CSVReader.readItems(ANSWERS_FILE_PATH, columns -> {
            String id = columns[0];
            String answerText = columns[1];
            boolean isCorrect = Boolean.parseBoolean(columns[2]);
            return new Answer(id, answerText, isCorrect);
        }, 0);

        answersById.forEach((id, answers) -> {
            System.out.println("ID: " + id);
            answers.forEach(System.out::println);
        });
        return answersById;
    }

    private static Map<String, ArrayList<Question>> getQuestions() {
        Map<String, ArrayList<Question>> questionsByCategory = CSVReader.readItems(QUESTIONS_FILE_PATH, columns -> {
            String id = columns[0];
            String category = columns[1];
            String questionText = columns[2];
            return new Question(id, category, questionText);
        }, 1);

        questionsByCategory.forEach((category, questions) -> {
            System.out.println("Category: " + category);
            questions.forEach(System.out::println);
        });
        return questionsByCategory;
    }


}
