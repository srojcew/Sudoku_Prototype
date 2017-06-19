package com.example.user.sudoku.backend.heuristics;

import java.util.Vector;

import com.example.user.sudoku.backend.heuristics.lessons.LessonFunctions;

/*
 * Stephen Rojcewicz
 *
 * Represents a row, column, or region. Does not contain cell values
 *
 */

public class AreaStruct {
    public static final int ROW = -5, COL = -6, REG = -7;
    private int areaType, area;
    private String areaTypeName;
    private Vector<Integer> cells;

    public AreaStruct(int at, int ar) {
        areaType = at;
        area = ar;
        cells = new Vector<Integer>();
        switch (areaType) {
            case ROW:
                for (int cell = 9 * area; cell < (9 * area) + 9; cell++) {
                    cells.add(cell);
                }
                areaTypeName = "row";
                break;
            case COL:
                for (int cell = area; cell < 81; cell += 9) {
                    cells.add(cell);
                }
                areaTypeName = "column";
                break;
            default:
                for (int i = (area / 3) * 3; i < (area / 3) * 3 + 3; i++) {
                    for (int j = (area % 3) * 3; j < (area % 3) * 3 + 3; j++) {
                        cells.add(9 * i + j);
                    }
                }
                areaTypeName = "region";

        }
    }

    public AreaStruct(AreaStruct aStruct) {
        areaType = aStruct.getType();
        area = aStruct.getArea();
        areaTypeName = new String(aStruct.getName());
        cells = LessonFunctions.copyVec(aStruct.getCells());
    }

    public int getType() {
        return areaType;
    }
    public int getArea() {
        return area;
    }
    public String getName() {
        return areaTypeName;
    }

    public Vector<Integer> getCells() {
        return cells;
    }
}
