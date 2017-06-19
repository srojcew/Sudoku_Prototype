package com.example.user.sudoku.backend;

import java.util.Vector;

import com.example.user.sudoku.backend.heuristics.lessons.LessonFunctions;

/*
 * Stephen Rojcewicz
 *
 * Represents the UI of a particular lesson step
 *
 */

public class LessonFormatting {
    private String message;
    private Vector<Vector<Integer>> gridNumbers;
    private Vector<Integer> cellsToHighlightLight;
    private Vector<Integer> cellsToHighlightDark;
    private Vector<Integer> cellsToHighlightRed;
    private Vector<Integer> candidateFormatCells;
    private Vector<Integer> editableCells;

    public LessonFormatting(String mesg, Vector<Vector<Integer>> numbers, Vector<Integer> lightCells, Vector<Integer> darkCells, Vector<Integer> redCells, Vector<Integer> candFormatCells, Vector<Integer> editableCs) {
        message = mesg;
        gridNumbers = numbers;
        cellsToHighlightLight = lightCells;
        cellsToHighlightDark = darkCells;
        cellsToHighlightRed = redCells;
        candidateFormatCells = candFormatCells;
        editableCells = editableCs;

        if (cellsToHighlightLight == null) {
            cellsToHighlightLight = new Vector<Integer>();
        }
        if (cellsToHighlightDark == null) {
            cellsToHighlightDark = new Vector<Integer>();
        }
        if (cellsToHighlightRed == null) {
            cellsToHighlightRed = new Vector<Integer>();
        }
        if (candidateFormatCells == null) {
            candidateFormatCells = new Vector<Integer>();
        }
        if (editableCells == null) {
            editableCells = new Vector<Integer>();
        }
    }

    public LessonFormatting(LessonFormatting formatting) {
        message = new String(formatting.getMessage());
        gridNumbers = LessonFunctions.copyCands(formatting.getIntGridNumbers());
        cellsToHighlightLight = LessonFunctions.copyVec(formatting.getCellsToHighlightLight());
        cellsToHighlightDark = LessonFunctions.copyVec(formatting.getCellsToHighlightDark());
        cellsToHighlightRed = LessonFunctions.copyVec(formatting.getCellsToHighlightRed());
        candidateFormatCells = LessonFunctions.copyVec(formatting.getCandidateFormatCells());
        editableCells = LessonFunctions.copyVec(formatting.getEditableCells());

    }

    public String[] getGridNumbers() {
        return Backend.candidatesIntToString(gridNumbers);
    }

    public Vector<Integer> getCellsToHighlightLight() {
        return cellsToHighlightLight;
    }

    public Vector<Integer> getCellsToHighlightDark() {
        return cellsToHighlightDark;
    }

    public Vector<Integer> getCellsToHighlightRed() {
        return cellsToHighlightRed;
    }

    public Vector<Integer> getCandidateFormatCells() {
        return candidateFormatCells;
    }

    public Vector<Integer> getEditableCells() {
        return editableCells;
    }

    public String getMessage() {
        return message;
    }

    private Vector<Vector<Integer>> getIntGridNumbers() {
        return gridNumbers;
    }
}
