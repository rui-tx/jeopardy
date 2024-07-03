package com.mindera.mindswap.board;

import java.util.List;


public class Cell {
    Question question;
    List<Answer> answers;

    private Cell(Question question, List<Answer> answers) {
        this.question = question;
        this.answers = answers;
    }

    public static Cell createCell(Question question, List<Answer> answers) {
        return new Cell(question, answers);
    }
}
