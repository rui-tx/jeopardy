package com.mindera.mindswap.board;

import com.mindera.mindswap.utils.CSVReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mindera.mindswap.Constants.*;
import static com.mindera.mindswap.utils.TerminalColors.*;


public class Board {
    private final Cell[][] gameBoard;

    public Board() {
        gameBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        Map<String, ArrayList<Question>> questionsByCategory = getQuestions();
        Map<String, ArrayList<Answer>> answersById = getAnswers();
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

    public boolean checkAnswerBool(int questionNumber, int selectedAnswer) {
        Cell cell = getCellByQuestionNumber(questionNumber);

        if (selectedAnswer <= 0 || selectedAnswer > cell.answers.size()) {
            return false;
        }
        Answer answer = cell.answers.get(selectedAnswer - 1);
        return answer.isCorrect;

    }

    private StringBuilder promptQuestionAndAnswers(Cell cell) {
        StringBuilder sb = new StringBuilder();

        sb.append(cell.question.questionValue).append(System.lineSeparator()).append(cell.question.questionText).append(System.lineSeparator());

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
            return ANSI_YELLOW + "Invalid question selection. That question already been answered, choose another." + ANSI_RESET + System.lineSeparator();
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

    public String displayPrettyBoard() {
        StringBuilder prettyBoard = new StringBuilder();
        int counter = 1;

        // hacky way to get the category names
        String[] catNames = new String[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            catNames[i] = gameBoard[0][i].question.category;
        }

        prettyBoard.append("\n");
        prettyBoard.append(ANSI_CYAN + " =================================================================================== \n" + ANSI_RESET);
        String format = ANSI_PURPLE + "| %-18s | %-18s | %-18s | %-18s | " + ANSI_RESET + "\n";
        prettyBoard.append(String.format(format, catNames[0], catNames[1], catNames[2], catNames[3]));
        prettyBoard.append(ANSI_CYAN + " =================================================================================== \n" + ANSI_RESET);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    format = "| Question " + ANSI_GREEN + "%-2d" + ANSI_RESET + " - %3s ";
                    prettyBoard.append(String.format(format, counter, gameBoard[row][col].question.questionValue));
                    counter++;
                } else {
                    format = "| " + ANSI_RED + "------------------ " + ANSI_RESET;
                    prettyBoard.append(String.format(format, counter, gameBoard[row][col].question.questionValue));
                }
            }
            prettyBoard.append("|\n");
        }

        prettyBoard.append(ANSI_CYAN + " =================================================================================== \n" + ANSI_RESET);
        return prettyBoard.toString();
    }

    private void populateBoardWithQuestionsAndAnswers(Map<String, ArrayList<Question>> questionsByCategory, Map<String, ArrayList<Answer>> answersById) {
        List<String> categories = new ArrayList<>(questionsByCategory.keySet());

        IntStream.range(0, BOARD_SIZE).forEach(col -> {
            if (col < categories.size()) {
                String category = categories.get(col);
                ArrayList<Question> questions = questionsByCategory.get(category);

                // Shuffle the questions to pick random ones
                Collections.shuffle(questions);

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
