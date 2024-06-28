package com.mindera.mindswap;

import com.mindera.mindswap.board.Answer;
import com.mindera.mindswap.board.Board;
import com.mindera.mindswap.board.Question;

import java.util.ArrayList;
import java.util.Map;

import static com.mindera.mindswap.Constants.ANSWERS_FILE_PATH;
import static com.mindera.mindswap.Constants.QUESTIONS_FILE_PATH;

public class Main {
    public static void main(String[] args) {

        Map<String, ArrayList<Question>> questionsByCategory = Question.CSVReader.readQuestions(QUESTIONS_FILE_PATH);
        System.out.println(questionsByCategory.keySet());

        Map<String, ArrayList<Answer>> answersById = Answer.CSVReader.readAnswers(ANSWERS_FILE_PATH);
        System.out.println(answersById.keySet());

        Board gameBoard = new Board(questionsByCategory, answersById);
        gameBoard.displayBoard();

        System.out.println(questionsByCategory.keySet());
        System.out.println(answersById.keySet());

        System.out.println();

        // Example of selecting a cell (1st row, 2nd column)
        gameBoard.selectCell(0, 0);
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

    }

}

