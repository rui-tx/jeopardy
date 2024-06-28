package com.mindera.mindswap.board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Answer {
    String id;
    String answerText;
    boolean isCorrect;

    public Answer(String id, String answerText, boolean isCorrect) {
        this.id = id;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    public class CSVReader {
        public static Map<String, ArrayList<Answer>> readAnswers(String csvFile) {
            Map<String, ArrayList<Answer>> answersById = new HashMap<>();
            String line;
            String csvSplitBy = ",";

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(csvSplitBy);
                    if (columns.length >= 3) {
                        String id = columns[0];
                        String answerText = columns[1];
                        boolean isCorrect = Boolean.parseBoolean(columns[2]);

                        Answer answer = new Answer(id, answerText, isCorrect);

                        answersById.computeIfAbsent(id, k -> new ArrayList<>()).add(answer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return answersById;
        }
    }
}
