package com.example.user.sudoku.backend;

import java.util.Vector;

import com.example.user.sudoku.backend.heuristics.lessons.LessonFunctions;

/*
 * Stephen Rojcewicz
 *
 * The user's response to a Lesson Step
 *
 */

public class LessonResponse {
    private Vector<Vector<Integer>> grid;
    private Integer cellClicked;

    public LessonResponse(String[] gd, Integer cell) {
        grid = Backend.candidatesStringToInt(gd);
        cellClicked = cell;
    }

    public LessonResponse(LessonResponse response) {
        grid = LessonFunctions.copyCands(response.getIntegerGrid());
        if (response.getCellClicked() == null) {
            cellClicked = null;
        }
        else {
            cellClicked = new Integer(response.getCellClicked());
        }
    }

    public LessonResponse(Vector<Vector<Integer>> gd, Integer cell) {
        grid = gd;
        cellClicked = cell;
    }

    public Vector<Vector<Integer>> getIntegerGrid() {
        return grid;
    }

    public Integer getCellClicked() {
        return cellClicked;
    }

    public boolean hasCellClicked() {
        return cellClicked != null;
    }
}
