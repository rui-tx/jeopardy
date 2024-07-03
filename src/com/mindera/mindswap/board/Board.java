package com.mindera.mindswap.board;

import com.mindera.mindswap.utils.CSVReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mindera.mindswap.Constants.*;
import static com.mindera.mindswap.utils.TerminalColors.*;


public class Board {
    private final Cell[][] gameBoard;
    private final Map<String, ArrayList<Question>> questionsByCategory;
    private final Map<String, ArrayList<Answer>> answersById;

    public Board() {
        gameBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        questionsByCategory = getQuestions();
        answersById = getAnswers();
        populateBoardWithQuestionsAndAnswers(questionsByCategory, answersById);
    }


    public String checkAnswer(int questionNumber, int selectedAnswer) {
        Cell cell = getCellByQuestionNumber(questionNumber);

        if (selectedAnswer <= 0 || selectedAnswer > cell.answers.size()) {
            return ANSI_YELLOW + "Invalid answer selection. Please select a valid answer (1-4): " + ANSI_RESET + System.lineSeparator();
        }
        Answer answer = cell.answers.get(selectedAnswer - 1);
        if (answer.isCorrect) {
            return ANSI_GREEN + "Correct answer!" + ANSI_RESET + System.lineSeparator();
        } else {
            return ANSI_RED + "Incorrect answer." + ANSI_RESET + System.lineSeparator();
        }
    }

    private StringBuilder promptQuestionAndAnswers(Cell cell) {
        StringBuilder sb = new StringBuilder();

        sb.append("Question: ").append(cell.question.questionText).append(System.lineSeparator());

        int optionNumber = 1;
        for (Answer answer : cell.answers) {
            sb.append(optionNumber).append(": ").append(answer.answerText).append(System.lineSeparator());
            optionNumber++;
        }
        return sb;
    }

    public String selectQuestion(int questionNumber) {
        Cell cell = getCellByQuestionNumber(questionNumber);
        if (cell == null) {
            return ANSI_YELLOW + "Invalid question selection. That question already been answered, choose another " + ANSI_RESET + System.lineSeparator();
        }
        return String.valueOf(promptQuestionAndAnswers(cell));
    }

    public Cell getCellByQuestionNumber(int questionNumber) {
        int counter = 1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    if (counter == questionNumber) {
                        return gameBoard[row][col];
                    }
                    counter++;
                }
            }
        }
        return null;
    }

    public String displayBoard() {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    sb.append("Question ").append(counter).append(" ");
                    counter++;
                }
            }
        }
        return String.valueOf(sb);
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
                        gameBoard[row][col] = Cell.createCell(question, answers);
                    } else {
                        gameBoard[row][col] = null;
                    }
                });
            }
        });
    }

    private Map<String, ArrayList<Answer>> getAnswers() {
        return CSVReader.readItems(ANSWERS_FILE_PATH, columns -> {
            String id = columns[0];
            String answerText = columns[1];
            boolean isCorrect = Boolean.parseBoolean(columns[2]);
            return new Answer(id, answerText, isCorrect);
        }, 0);
    }

    private Map<String, ArrayList<Question>> getQuestions() {
        return CSVReader.readItems(QUESTIONS_FILE_PATH, columns -> {
            String id = columns[0];
            String category = columns[1];
            String questionValue = columns[2];
            String questionText = columns[3];
            return new Question(id, category, questionValue, questionText);
        }, 1);
    }
}
