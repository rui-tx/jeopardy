package com.mindera.mindswap.board;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Question {
    String id;
    String category;
    String questionText;

    public Question(String id, String category, String questionText) {
        this.id = id;
        this.category = category;
        this.questionText = questionText;
    }

    public class CSVReader {
        public static Map<String, ArrayList<Question>> readQuestions(String csvFile) {
            Map<String, ArrayList<Question>> questionsByCategory = new HashMap<>();
            String line;

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length >= 3) {
                        String id = columns[0];
                        String category = columns[1];
                        String questionText = columns[2];

                        Question question = new Question(id, category, questionText);

                        questionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return questionsByCategory;
        }
    }


    public String getCategory() {
        return category;
    }
}
