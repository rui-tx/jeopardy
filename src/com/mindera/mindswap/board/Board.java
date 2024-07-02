package com.mindera.mindswap.board;

import com.mindera.mindswap.utils.CSVReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mindera.mindswap.Constants.*;


public class Board {
    private final Cell[][] gameBoard;
    private final Map<String, ArrayList<Question>> questionsByCategory;
    private final Map<String, ArrayList<Answer>> answersById;
    private final StringBuilder sb;

    public Board() {
        gameBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        sb = new StringBuilder();
        questionsByCategory = getQuestions();
        answersById = getAnswers();
        populateBoardWithQuestionsAndAnswers(questionsByCategory, answersById);
    }


    public String checkAnswer(int questionNumber, int selectedAnswer) {
        Cell cell = getCellByQuestionNumber(questionNumber);

        if (selectedAnswer <= 0 || selectedAnswer > cell.answers.size()) {
            return "Invalid answer selection. Please select a valid answer (1-4): ";
        }
        Answer answer = cell.answers.get(selectedAnswer - 1);
        if (answer.isCorrect) {
            return "Correct answer!";
        } else {
            return "Incorrect answer.";
        }
    }

    private StringBuilder promptQuestionAndAnswers(Cell cell) {
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
            return "Invalid question selection.";
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

    public StringBuilder displayBoard() {
        int counter = 1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    sb.append("Question ").append(counter).append(" ");
                    counter++;
                }
            }
        }
        return sb;
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
            String questionText = columns[2];
            return new Question(id, category, questionText);
        }, 1);
    }
}
