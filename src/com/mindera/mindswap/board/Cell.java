package com.mindera.mindswap.board;

import java.util.ArrayList;


public class Cell {
    Question question;
    ArrayList<Answer> answers;

    public Cell(Question question, ArrayList<Answer> answers) {
        this.question = question;
        this.answers = answers;
    }
}
