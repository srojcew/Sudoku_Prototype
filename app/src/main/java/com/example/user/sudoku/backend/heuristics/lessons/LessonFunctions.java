package com.example.user.sudoku.backend.heuristics.lessons;

import java.util.Random;
import java.util.Vector;

import com.example.user.sudoku.backend.SudokuGraph;
import com.example.user.sudoku.backend.heuristics.*;

/*
 * Stephen Rojcewicz
 *
 * Utility functions used in processing lessons
 *
 */

public class LessonFunctions {


    static Vector<Vector<Integer>> applyHiddenSingle(Vector<Vector<Integer>> cands, CandsPattern hiddenSingle) {
        // make a copy of cands
        Vector<Vector<Integer>> candidates = copyCands(cands);
        int cell = hiddenSingle.getCells().firstElement();
        candidates.elementAt(cell).clear();
        candidates.elementAt(cell).add(hiddenSingle.getNums()[0]);
        return candidates;
    }

    private static Vector<Integer> findCellsInArea(int areaType, int area) {
        Vector<Integer> cells = new Vector<Integer>();
        if (areaType == AreaStruct.ROW) {
            for (int cell = 9 * area; cell < (9 * area) + 9; cell++) {
                cells.add(cell);
            }
        }
        else if (areaType == AreaStruct.COL) {
            for (int cell = area; cell < 81; cell += 9) {
                cells.add(cell);
            }
        }
        else {
            for (int i = (area / 3) * 3; i < (area / 3) * 3 + 3; i++) {
                for (int j = (area % 3) * 3; j < (area % 3) * 3 + 3; j++) {
                    cells.add(9 * i + j);
                }
            }
        }
        return cells;
    }

    /**
     *
     * @param candidates the candidates cells
     * @param num
     * @param ar
     * @return Vector containing the cell indices of the candidate cells that contain the num in area ar
     */
    static Vector<Integer> findCellsThatContain(Vector<Vector<Integer>> candidates, int num, AreaStruct ar) {
        int areaType = ar.getType();
        int area = ar.getArea();
        Vector<Integer> cells = new Vector<Integer>();
        if (areaType == AreaStruct.ROW) {
            for (int cell = 9 * area; cell < (9 * area) + 9; cell++) {
                if (candidates.elementAt(cell).contains(num)) {
                    cells.add(cell);
                }
            }
        }
        else if (areaType == AreaStruct.COL) {
            for (int cell = area; cell < 81; cell += 9) {
                if (candidates.elementAt(cell).contains(num)) {
                    cells.add(cell);
                }
            }
        }
        else {
            for (int i = (area / 3) * 3; i < (area / 3) * 3 + 3; i++) {
                for (int j = (area % 3) * 3; j < (area % 3) * 3 + 3; j++) {
                    if (candidates.elementAt(9 * i + j).contains(num)) {
                        cells.add(9 * i + j);
                    }
                }
            }
        }
        return cells;
    }

    /**
     * Modifies the candidates after a number has been committed
     * @param cands
     * @param committedNum
     * @param committedCell
     * @return
     */
    static Vector<Integer> updateCandidates(Vector<Vector<Integer>> cands, int committedNum, int committedCell) {
        Vector<Integer> cells = new Vector<Integer>();
        int row = committedCell / 9;
        int col = committedCell % 9;
        int reg = 3 * (row / 3) + col / 3;
        Vector<Integer> rowCells = findCellsInArea(AreaStruct.ROW, row);
        Vector<Integer> colCells = findCellsInArea(AreaStruct.COL, col);
        Vector<Integer> regCells = findCellsInArea(AreaStruct.REG, reg);

        for (int i = 0; i < 9; i++) {
            int rowCell = rowCells.elementAt(i);
            if (cands.elementAt(rowCell).size() > 1 && cands.elementAt(rowCell).contains(committedNum)) {
                cells.add(rowCell);
                cands.elementAt(rowCell).remove(new Integer(committedNum));
            }
            int colCell = colCells.elementAt(i);
            if (cands.elementAt(colCell).size() > 1 && cands.elementAt(colCell).contains(committedNum)) {
                cells.add(colCell);
                cands.elementAt(colCell).remove(new Integer(committedNum));
            }
            int regCell = regCells.elementAt(i);
            if (cands.elementAt(regCell).size() > 1 && cands.elementAt(regCell).contains(committedNum)) {
                cells.add(regCell);
                cands.elementAt(regCell).remove(new Integer(committedNum));
            }
        }
        return cells;
    }

    /**
     * chooses a CandsPattern whose type occurs just once in its area
     * @param patterns
     * @param rand
     * @return
     */
    static CandsPattern choosePatternUniqueInArea(Vector<CandsPattern> patterns, Random rand) {
        Vector<Vector<CandsPattern>> rowPatterns = new Vector<Vector<CandsPattern>>();
        Vector<Vector<CandsPattern>> colPatterns = new Vector<Vector<CandsPattern>>();
        Vector<Vector<CandsPattern>> regPatterns = new Vector<Vector<CandsPattern>>();
        for (int area = 0; area < 9; area++) {
            rowPatterns.add(new Vector<CandsPattern>());
            colPatterns.add(new Vector<CandsPattern>());
            regPatterns.add(new Vector<CandsPattern>());
        }

        for (int i = 0; i < patterns.size(); i++) {
            CandsPattern pattern = patterns.elementAt(i);
            AreaStruct area = pattern.getArea();
            switch (area.getType()) {
                case AreaStruct.ROW:
                    rowPatterns.elementAt(area.getArea()).add(pattern);
                    break;
                case AreaStruct.COL:
                    colPatterns.elementAt(area.getArea()).add(pattern);
                    break;
                default:
                    regPatterns.elementAt(area.getArea()).add(pattern);
                    break;
            }
        }

        Vector<CandsPattern> patternsUniqueInArea = new Vector<CandsPattern>();
        for (int area = 0; area < 9; area++) {
            if (rowPatterns.elementAt(area).size() == 1) { // there is just one pattern in this row
                patternsUniqueInArea.add(rowPatterns.elementAt(area).firstElement());
            }
            if (colPatterns.elementAt(area).size() == 1) {
                patternsUniqueInArea.add(colPatterns.elementAt(area).firstElement());
            }
            if (regPatterns.elementAt(area).size() == 1) {
                patternsUniqueInArea.add(regPatterns.elementAt(area).firstElement());
            }
        }
        if (patternsUniqueInArea.isEmpty()) {
            return null;
        }
        // randomly select one of the unique patterns
        return patternsUniqueInArea.elementAt(rand.nextInt(patternsUniqueInArea.size()));
    }

    static ConflictStruct findConflictingCell(int testCell, int testValue, Vector<Vector<Integer>> candidates) {
        int row = testCell / 9;
        int col = testCell % 9;
        int reg = 3 * (row / 3) + col / 3;
        Vector<Integer> rowCells = findCellsInArea(AreaStruct.ROW, row);
        Vector<Integer> colCells = findCellsInArea(AreaStruct.COL, col);
        Vector<Integer> regCells = findCellsInArea(AreaStruct.REG, reg);
        for (int i = 0; i < rowCells.size(); i++) {
            int cell = rowCells.elementAt(i);
            if (candidates.elementAt(cell).size() == 1 && cell != testCell && candidates.elementAt(cell).elementAt(0).equals(testValue)) {
                return new ConflictStruct(cell, new AreaStruct(AreaStruct.ROW, row));
            }
        }
        for (int i = 0; i < colCells.size(); i++) {
            int cell = colCells.elementAt(i);
            if (candidates.elementAt(cell).size() == 1 && cell != testCell && candidates.elementAt(cell).elementAt(0).equals(testValue)) {
                return new ConflictStruct(cell, new AreaStruct(AreaStruct.COL, col));
            }
        }
        for (int i = 0; i < regCells.size(); i++) {
            int cell = regCells.elementAt(i);
            if (candidates.elementAt(cell).size() == 1 && cell != testCell && candidates.elementAt(cell).elementAt(0).equals(testValue)) {
                return new ConflictStruct(cell, new AreaStruct(AreaStruct.REG, reg));
            }
        }
        return null;
    }

    public static Vector<Vector<Integer>> copyCands(Vector<Vector<Integer>> cands) {
        Vector<Vector<Integer>> copy = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            copy.add(new Vector<Integer>());
            for (int j = 0; j < cands.elementAt(i).size(); j++) {
                copy.elementAt(i).add(new Integer(cands.elementAt(i).elementAt(j)));
            }
        }
        return copy;
    }

    public static Vector<Integer> copyVec(Vector<Integer> vec) {
        Vector<Integer> copy = new Vector<Integer>();
        for (int i = 0; i < vec.size(); i++) {
            copy.add(vec.elementAt(i));
        }
        return copy;
    }
}
