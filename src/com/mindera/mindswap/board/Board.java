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

    /**
     * Create a new board
     * csv files are read and the questions and answers are populated into the board
     */
    public Board() {
        gameBoard = new Cell[BOARD_SIZE][BOARD_SIZE];
        Map<String, ArrayList<Question>> questionsByCategory = getQuestions();
        Map<String, ArrayList<Answer>> answersById = getAnswers();
        populateBoardWithQuestionsAndAnswers(questionsByCategory, answersById);
    }

    /**
     * Check if the game is over
     * @return true if the game is over
     */
    public boolean isGameOver() {
        int totalQuestions = BOARD_SIZE * BOARD_SIZE;
        int answeredQuestions = 0;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null && gameBoard[row][col].question.isAnswered) {
                    answeredQuestions++;
                }
            }
        }
        return answeredQuestions == totalQuestions;
    }

    /**
     * Check if the answer is correct
     * @param questionNumber the question number
     * @param selectedAnswer the selected answer
     * @return true if the answer is correct or false otherwise
     */
    public boolean checkAnswerBool(int questionNumber, int selectedAnswer) {
        Cell cell = getCellByQuestionNumber(questionNumber);
        if (selectedAnswer <= 0 || selectedAnswer > cell.answers.size()) {
            return false;
        }
        Answer answer = cell.answers.get(selectedAnswer - 1);
        return answer.isCorrect;
    }

    /**
     * Validate the answer
     * @param questionNumber the question number
     * @param selectedAnswer the selected answer
     * @return a string with the validation result
     */
    public String validateAnswer(int questionNumber, int selectedAnswer) {
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

    /**
     * Process the question
     * @param questionNumber the question number
     * @param markAsAnswered true if the question should be marked as answered
     * @return true if the question was processed successfully
     */
    public boolean processQuestionBoolean(int questionNumber, boolean markAsAnswered) {
        int counter = 1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (gameBoard[row][col] != null) {
                    if (counter == questionNumber) {
                        if (markAsAnswered) {
                            gameBoard[row][col].question.isAnswered = true;
                        }
                        return gameBoard[row][col].question.isAnswered;
                    }
                    counter++;
                }
            }
        }
        return false;
    }

    /**
     * Get the question value
     * @param questionNumber the question number
     * @return the question value
     */
    public Integer getQuestionValue(int questionNumber) {
        Cell cell = getCellByQuestionNumber(questionNumber);
        return Integer.parseInt(cell.question.questionValue);
    }

    /**
     * Prompt the user to select an answer
     * @param cell the cell
     * @return a string with the prompt
     */
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

    /**
     * Select a question
     * @param questionNumber the question number
     * @return a string with the question
     */
    public String selectQuestion(int questionNumber) {
        Cell cell = getCellByQuestionNumber(questionNumber);
        if (cell == null) {
            return ANSI_YELLOW + "That question already been answered, choose another." + ANSI_RESET + System.lineSeparator();
        }
        return String.valueOf(promptQuestionAndAnswers(cell));
    }

    /**
     * Get the cell by question number
     * @param questionNumber the question number
     * @return the cell
     */
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

    /**
     * Display the pretty board
     * @return a string with the pretty board
     */
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
                if (gameBoard[row][col] != null && !gameBoard[row][col].question.isAnswered) {
                    format = "| Question " + ANSI_GREEN + "%-2d" + ANSI_RESET + " - %3s ";
                    prettyBoard.append(String.format(format, counter, gameBoard[row][col].question.questionValue));
                    counter++;
                } else {
                    format = "| " + ANSI_RED + "----------------- " + ANSI_RESET;
                    prettyBoard.append(String.format(format, counter, gameBoard[row][col].question.questionValue));
                    counter++;
                }
            }
            prettyBoard.append("|\n");
        }

        prettyBoard.append(ANSI_CYAN + " =================================================================================== \n" + ANSI_RESET);
        return prettyBoard.toString();
    }

    /**
     * Populate the board with questions and answers
     * @param questionsByCategory each category has a list of questions
     * @param answersById         each question has a list of answers
     */
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

                        // Shuffle the multiple-choice answers order
                        Collections.shuffle(answers);

                        gameBoard[row][col] = Cell.createCell(question, answers);
                    } else {
                        gameBoard[row][col] = null;
                    }
                });
            }
        });
    }

    /**
     * Get the answers
     * @return a map with the answers
     */
    private Map<String, ArrayList<Answer>> getAnswers() {
        return CSVReader.readItems(ANSWERS_FILE_PATH, columns -> {
            String id = columns[0];
            String answerText = columns[1];
            boolean isCorrect = Boolean.parseBoolean(columns[2]);
            return new Answer(id, answerText, isCorrect);
        }, 0);
    }

    /**
     * Get the questions
     * @return a map with the questions
     */
    private Map<String, ArrayList<Question>> getQuestions() {
        return CSVReader.readItems(QUESTIONS_FILE_PATH, columns -> {
            String id = columns[0];
            String category = columns[1];
            String questionValue = columns[2];
            String questionText = columns[3];
            boolean isAnswered = Boolean.parseBoolean(columns[4]);
            return new Question(id, category, questionValue, questionText, isAnswered);
        }, 1);
    }
}
