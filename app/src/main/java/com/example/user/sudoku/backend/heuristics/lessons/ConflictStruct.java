package com.example.user.sudoku.backend.heuristics.lessons;

import com.example.user.sudoku.backend.heuristics.AreaStruct;

/*
 * Stephen Rojcewicz
 *
 * Represents an invalid cell in a specific area
 *
 */

class ConflictStruct {
    private int cell;
    private AreaStruct area;

    public ConflictStruct(int c, AreaStruct ar) {
        cell = c;
        area = ar;
    }
    public int getCell() {
        return cell;
    }
    public AreaStruct getArea() {
        return area;
    }
}
