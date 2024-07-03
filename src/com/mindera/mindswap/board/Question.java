package com.mindera.mindswap.board;


public class Question {
    String id;
    String category;
    String questionValue;
    String questionText;


    public Question(String id, String category, String questionValue, String questionText) {
        this.id = id;
        this.category = category;
        this.questionValue = questionValue;
        this.questionText = questionText;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", questionValue='" + questionValue + '\'' +
                ", questionText='" + questionText + '\'' +
                '}';
    }
}
