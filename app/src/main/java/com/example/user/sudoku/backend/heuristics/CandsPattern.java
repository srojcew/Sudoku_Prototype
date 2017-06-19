package com.example.user.sudoku.backend.heuristics;

import java.util.Vector;

import com.example.user.sudoku.backend.heuristics.lessons.LessonFunctions;

/*
 * Stephen Rojcewicz
 *
 * Represents a candidates pattern of a given heuristic type
 *
 */
public class CandsPattern {
    private int type;
    private  AreaStruct area;
    int[] nums;
    Vector<Integer> cells;

    public CandsPattern(int typ, AreaStruct ar, int[] ns, Vector<Integer> cls) {
        type = typ;
        area = ar;
        nums = ns;
        cells = cls;
    }

    public CandsPattern(CandsPattern pattern) {
        type = pattern.getType();
        if (pattern.getArea() == null) {
            area = null;
        }
        else {
            area = new AreaStruct(pattern.getArea());
        }
        nums = pattern.getNums();
        cells = LessonFunctions.copyVec(pattern.getCells());
    }

    public int getType() {
        return type;
    }

    public AreaStruct getArea() {
        return area;
    }

    public int[] getNums() {
        return nums;
    }

    public Vector<Integer> getCells() {
        return cells;
    }

}
