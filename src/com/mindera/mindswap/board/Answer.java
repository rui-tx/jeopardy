package com.mindera.mindswap.board;


public class Answer {
    String id;
    String answerText;
    boolean isCorrect;

    public Answer(String id, String answerText, boolean isCorrect) {
        this.id = id;
        this.answerText = answerText;
        this.isCorrect = isCorrect;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id='" + id + '\'' +
                ", answerText='" + answerText + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
