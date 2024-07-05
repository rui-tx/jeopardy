package com.mindera.mindswap.board;


public class Question {
    String id;
    String category;
    String questionValue;
    String questionText;
    boolean isAnswered;

    public Question(String id, String category, String questionValue, String questionText, boolean isAnswered) {
        this.id = id;
        this.category = category;
        this.questionValue = questionValue;
        this.questionText = questionText;
        this.isAnswered = isAnswered;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", category='" + category + '\'' +
                ", questionValue='" + questionValue + '\'' +
                ", questionText='" + questionText + '\'' +
                ", isAnswered=" + isAnswered +
                '}';
    }
}
