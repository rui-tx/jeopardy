package com.mindera.mindswap.board;


public class Question {
    String id;
    String category;
    String questionText;

    public Question(String id, String category, String questionText) {
        this.id = id;
        this.category = category;
        this.questionText = questionText;
    }


    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getQuestionText() {
        return questionText;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", questionText='" + questionText + '\'' +
                '}';
    }
}
