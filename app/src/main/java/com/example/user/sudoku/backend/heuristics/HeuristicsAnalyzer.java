package com.example.user.sudoku.backend.heuristics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import com.example.user.sudoku.backend.TypeConstants;


/*
 * Stephen Rojcewicz
 *
 * Contains the implementation of the heuristics.
 *
 */

public class HeuristicsAnalyzer {

    private static final int SUBSET_1 = -321;
    private static final int SUBSET_2 = -322;
    private static final int SUBSET_3 = -323;
    private static final int IMPROP_SUBSET = -324;

    /**
     *
     * @param allCandidates Vector of Vectors of candidates indexed by cell number. The caller is responsible for checking the
     * validity of the candidates. Note that a cell may have an empty Vector of candidates
     * @return
     */
    public static Vector<CandsPattern> findNakedSingles(Vector<Vector<Integer>> allCandidates, int[] committedNums) {
        HashMap<Integer, Vector<Integer>> singlesCells = new HashMap<Integer, Vector<Integer>>();
        for (int i = 0; i < 9; i++) {
            singlesCells.put(i, new Vector<Integer>());
        }
        for (int i = 0; i < 81; i++) {
            if (allCandidates.elementAt(i).size() == 1) {
                // check if the naked single has already been committed
                if (allCandidates.elementAt(i).firstElement() != committedNums[i]) {
                    singlesCells.get(allCandidates.elementAt(i).firstElement()).add(i);
                }
            }
        }
        Vector<CandsPattern> nakedSingles = new Vector<CandsPattern>();
        for (int i = 0; i < 9; i++) {
            if (!singlesCells.get(i).isEmpty()) {
                int[] nums = new int[1];
                nums[0] = i;
                for (int j = 0; j < singlesCells.get(i).size(); j++) {
                    Vector<Integer> cell = new Vector<Integer>();
                    cell.add(singlesCells.get(i).elementAt(j));
                    nakedSingles.add(new CandsPattern(TypeConstants.N_SINGLE, null, nums, cell));
                }
            }
        }
        return nakedSingles;
    }

    public static Vector<CandsPattern> findHiddenSingles(Vector<Vector<Integer>> allCandidates) {
        Vector<Vector<Vector<Integer>>> singlesRowCells = new Vector<Vector<Vector<Integer>>>();
        Vector<Vector<Vector<Integer>>> singlesColCells = new Vector<Vector<Vector<Integer>>>();
        Vector<Vector<Vector<Integer>>> singlesRegCells = new Vector<Vector<Vector<Integer>>>();
        for (int i = 0; i < 9; i++) {
            singlesRowCells.add(new Vector<Vector<Integer>>());
            singlesColCells.add(new Vector<Vector<Integer>>());
            singlesRegCells.add(new Vector<Vector<Integer>>());
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                singlesRowCells.elementAt(i).add(new Vector<Integer>());
                singlesColCells.elementAt(i).add(new Vector<Integer>());
                singlesRegCells.elementAt(i).add(new Vector<Integer>());
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cell = 9 * row + col;
                Vector<Integer> cands = allCandidates.elementAt(cell);
                int reg = (row / 3) * 3 + (col / 3);

                // do not report naked singles
                if (cands.size() == 1) {
                    for (int i = 0; i < 3; i++) {
                        int cand = cands.firstElement();
                        singlesRowCells.elementAt(cand).elementAt(row).add(TypeConstants.BLANK);
                        singlesColCells.elementAt(cand).elementAt(col).add(TypeConstants.BLANK);
                        singlesRegCells.elementAt(cand).elementAt(reg).add(TypeConstants.BLANK);
                    }
                }
                else {
                    for (int i = 0; i < cands.size(); i++) {
                        int cand = cands.elementAt(i);
                        singlesRowCells.elementAt(cand).elementAt(row).add(cell);
                        singlesColCells.elementAt(cand).elementAt(col).add(cell);
                        singlesRegCells.elementAt(cand).elementAt(reg).add(cell);
                    }
                }
            }
        }

        Vector<CandsPattern> hiddenSingles = new Vector<CandsPattern>();
        for (int cand = 0; cand < 9; cand++) {
            int[] nums = new int[1];
            nums[0] = cand;
            for (int area = 0; area < 9; area++) {
                if (singlesRowCells.elementAt(cand).elementAt(area).size() == 1) { // cand occurs in row i just once
                    hiddenSingles.add(new CandsPattern(TypeConstants.H_SINGLE, new AreaStruct(AreaStruct.ROW, area), nums, singlesRowCells.elementAt(cand).elementAt(area)));
                }
                if (singlesColCells.elementAt(cand).elementAt(area).size() == 1) {
                    hiddenSingles.add(new CandsPattern(TypeConstants.H_SINGLE, new AreaStruct(AreaStruct.COL, area), nums, singlesColCells.elementAt(cand).elementAt(area)));
                }
                if (singlesRegCells.elementAt(cand).elementAt(area).size() == 1) {
                    hiddenSingles.add(new CandsPattern(TypeConstants.H_SINGLE, new AreaStruct(AreaStruct.REG, area), nums, singlesRegCells.elementAt(cand).elementAt(area)));
                }
            }
        }
        return hiddenSingles;
    }

    public static Vector<CandsPattern> findNakedTriples(Vector<Vector<Integer>> candidates, boolean modify) {
        // SudokuTriple objects indexed by the SudokuTriple's numbers
        SudokuTriple[][][] triples = new SudokuTriple[9][9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                for (int k = j + 1; k < 9; k++) {
                    triples[i][j][k] = new SudokuTriple(i, j, k);
                }
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cell = 9 * row + col;
                Vector<Integer> cands = candidates.elementAt(cell);
                int reg = (row / 3) * 3 + (col / 3);
                // sorted list of triple numbers
                TreeSet<Integer> sortedNums = new TreeSet<Integer>();
                if (cands.size() == 2) {
                    for (int i = 0; i < 9; i++) {
                        if (i != cands.firstElement() && i != cands.elementAt(1)) {
                            sortedNums.clear();
                            sortedNums.add(cands.firstElement());
                            sortedNums.add(cands.elementAt(1));
                            sortedNums.add(i);
                            Iterator<Integer> iter = sortedNums.iterator();
                            int first = iter.next();
                            int second = iter.next();
                            int third = iter.next();
                            int subset;
                            if (cands.contains(first) && cands.contains(second)) {
                                subset = 0;
                            }
                            else if (cands.contains(first) && cands.contains(third)) {
                                subset = 1;
                            }
                            else if (cands.contains(second) && cands.contains(third)){
                                subset = 2;
                            }
                            else {
                                System.err.println("invalid in findNakedTriples");
                                subset = -1;
                                System.exit(1);
                            }
                            // must note the subset because a triple need not contain all three numbers
                            triples[first][second][third].addRowCell(row, cell, subset);
                            triples[first][second][third].addColCell(col, cell, subset);
                            triples[first][second][third].addRegCell(reg, cell, subset);
                        }
                    }
                }
                else if (cands.size() == 3) {
                    sortedNums.clear();
                    for (int i = 0; i < 3; i++) {
                        sortedNums.add(cands.elementAt(i));
                    }
                    Iterator<Integer> iter = sortedNums.iterator();
                    int first = iter.next();
                    int second = iter.next();
                    int third = iter.next();
                    triples[first][second][third].addRowCell(row, cell, 3);
                    triples[first][second][third].addColCell(col, cell, 3);
                    triples[first][second][third].addRegCell(reg, cell, 3);
                }
            }
        }
        Vector<CandsPattern> nakedTriples = new Vector<CandsPattern>();
        for (int num1 = 0; num1 < 9; num1++) {
            for (int num2 = num1 + 1; num2 < 9; num2++) {
                for (int num3 = num2 + 1; num3 < 9; num3++) {
                    int[] nums = new int[3];
                    nums[0] = num1;
                    nums[1] = num2;
                    nums[2] = num3;
                    for (int area = 1; area < 9; area++) {
                        Vector<Integer> rowCells = triples[num1][num2][num3].getRowCells(area);
                        Vector<Integer> colCells = triples[num1][num2][num3].getColCells(area);
                        Vector<Integer> regCells = triples[num1][num2][num3].getRegCells(area);
                        if (rowCells.size() == 3) {
                            CandsPattern nakedTriple = new CandsPattern(TypeConstants.N_TRIPLE, new AreaStruct(AreaStruct.ROW, area), nums, rowCells);
                            if (applyNakedTuple(nakedTriple, candidates, modify)) {
                                nakedTriples.add(nakedTriple);
                            }
                        }
                        if (colCells.size() == 3) {
                            CandsPattern nakedTriple = new CandsPattern(TypeConstants.N_TRIPLE, new AreaStruct(AreaStruct.COL, area), nums, colCells);
                            if (applyNakedTuple(nakedTriple, candidates, modify)) {
                                nakedTriples.add(nakedTriple);
                            }
                        }
                        if (regCells.size() == 3) {
                            CandsPattern nakedTriple = new CandsPattern(TypeConstants.N_TRIPLE, new AreaStruct(AreaStruct.REG, area), nums, regCells);
                            if (applyNakedTuple(nakedTriple, candidates, modify)) {
                                nakedTriples.add(nakedTriple);
                            }
                        }
                    }
                }
            }
        }
        return nakedTriples;
    }

    public static Vector<CandsPattern> findNakedPairs(Vector<Vector<Integer>> candidates, boolean modify) {
        SudokuPair[][] pairs = new SudokuPair[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                pairs[i][j] = new SudokuPair(i, j);
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cell = 9 * row + col;
                Vector<Integer> cands = candidates.elementAt(cell);
                int reg = (row / 3) * 3 + (col / 3);
                if (cands.size() == 2) {
                    int first, second;
                    if (cands.firstElement() < cands.elementAt(1)) {
                        first = cands.firstElement();
                        second = cands.elementAt(1);
                    }
                    else {
                        first = cands.elementAt(1);
                        second = cands.firstElement();
                    }
                    pairs[first][second].addRowCell(row, cell);
                    pairs[first][second].addColCell(col, cell);
                    pairs[first][second].addRegCell(reg, cell);
                }
            }
        }
        Vector<CandsPattern> nakedPairs = new Vector<CandsPattern>();
        for (int num1 = 0; num1 < 9; num1++) {
            for (int num2 = num1 + 1; num2 < 9; num2++) {
                int[] nums = new int[2];
                nums[0] = num1;
                nums[1] = num2;
                for (int area = 1; area < 9; area++) {
                    Vector<Integer> rowCells = pairs[num1][num2].getRowCells(area);
                    Vector<Integer> colCells = pairs[num1][num2].getColCells(area);
                    Vector<Integer> regCells = pairs[num1][num2].getRegCells(area);
                    if (rowCells.size() == 2) {
                        CandsPattern nakedPair = new CandsPattern(TypeConstants.N_PAIR, new AreaStruct(AreaStruct.ROW, area), nums, rowCells);
                        if (applyNakedTuple(nakedPair, candidates, modify)) {
                            nakedPairs.add(nakedPair);
                        }
                    }
                    if (colCells.size() == 2) {
                        CandsPattern nakedPair = new CandsPattern(TypeConstants.N_PAIR, new AreaStruct(AreaStruct.COL, area), nums, colCells);
                        if (applyNakedTuple(nakedPair, candidates, modify)) {
                            nakedPairs.add(nakedPair);
                        }
                    }
                    if (regCells.size() == 2) {
                        CandsPattern nakedPair = new CandsPattern(TypeConstants.N_PAIR, new AreaStruct(AreaStruct.REG, area), nums, regCells);
                        if (applyNakedTuple(nakedPair, candidates, modify)) {
                            nakedPairs.add(nakedPair);
                        }
                    }
                }
            }
        }
        return nakedPairs;
    }

    public static Vector<CandsPattern> findHiddenPairs(Vector<Vector<Integer>> candidates, boolean modify) {
        SudokuPair[][] pairs = new SudokuPair[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                pairs[i][j] = new SudokuPair(i, j);
            }
        }

        int[][] singlesRowCounts = new int[9][9];
        int[][] singlesColCounts = new int[9][9];
        int[][] singlesRegCounts = new int[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cell = 9 * row + col;
                Vector<Integer> cands = candidates.elementAt(cell);
                int reg = (row / 3) * 3 + (col / 3);
                for (int i = 0; i < cands.size(); i++) {
                    singlesRowCounts[cands.elementAt(i)][row]++;
                    singlesColCounts[cands.elementAt(i)][col]++;
                    singlesRegCounts[cands.elementAt(i)][reg]++;
                    for (int j = i + 1; j < cands.size(); j++) {
                        int first, second;
                        if (cands.elementAt(i) < cands.elementAt(j)) {
                            first = cands.elementAt(i);
                            second = cands.elementAt(j);
                        }
                        else {
                            first = cands.elementAt(j);
                            second = cands.elementAt(i);
                        }
                        pairs[first][second].addRowCell(row, cell);
                        pairs[first][second].addColCell(col, cell);
                        pairs[first][second].addRegCell(reg, cell);
                    }
                }
            }
        }
        Vector<CandsPattern> hiddenPairs = new Vector<CandsPattern>();
        for (int num1 = 0; num1 < 9; num1++) {
            for (int num2 = num1 + 1; num2 <9; num2++) {
                int[] nums = new int[2];
                nums[0] = num1;
                nums[1] = num2;
                for (int area = 1; area < 9; area++) {
                    Vector<Integer> rowCells = pairs[num1][num2].getRowCells(area);
                    Vector<Integer> colCells = pairs[num1][num2].getColCells(area);
                    Vector<Integer> regCells = pairs[num1][num2].getRegCells(area);
                    if (rowCells.size() == 2 && singlesRowCounts[num1][area] == 2 && singlesRowCounts[num2][area] == 2) {
                        CandsPattern hiddenPair = new CandsPattern(TypeConstants.H_PAIR, new AreaStruct(AreaStruct.ROW, area), nums, rowCells);
                        // return only those hidden pairs that result in simplification
                        if (applyHiddenTuple(hiddenPair, candidates, modify)) {
                            hiddenPairs.add(hiddenPair);
                        }
                    }
                    if (colCells.size() == 2 && singlesColCounts[num1][area] == 2 && singlesColCounts[num2][area] == 2) {
                        CandsPattern hiddenPair = new CandsPattern(TypeConstants.H_PAIR, new AreaStruct(AreaStruct.COL, area), nums, colCells);
                        if (applyHiddenTuple(hiddenPair, candidates, modify)) {
                            hiddenPairs.add(hiddenPair);
                        }
                    }
                    if (regCells.size() == 2 && singlesRegCounts[num1][area] == 2 && singlesRegCounts[num2][area] == 2) {
                        CandsPattern hiddenPair = new CandsPattern(TypeConstants.H_PAIR, new AreaStruct(AreaStruct.REG, area), nums, regCells);
                        if (applyHiddenTuple(hiddenPair, candidates, modify)) {
                            hiddenPairs.add(hiddenPair);
                        }
                    }
                }
            }
        }
        return hiddenPairs;
    }

    public static Vector<CandsPattern> findHiddenTriples(Vector<Vector<Integer>> candidates, boolean modify) {
        // SudokuTriple objects indexed by the SudokuTriple's numbers
        SudokuTriple[][][] triples = new SudokuTriple[9][9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = i + 1; j < 9; j++) {
                for (int k = j + 1; k < 9; k++) {
                    triples[i][j][k] = new SudokuTriple(i, j, k);
                }
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int cell = 9 * row + col;
                Vector<Integer> cands = candidates.elementAt(cell);
                int reg = (row / 3) * 3 + (col / 3);
                // sorted list of triple numbers
                for (int i = 0; i < 9; i++) {
                    for (int j = i + 1; j < 9; j++) {
                        for (int k = j + 1; k < 9; k++) {
                            // must note the subset because a triple need not contain all three numbers
                            int subset;
                            if (cands.contains(i) && cands.contains(j) && cands.contains(k)) {
                                subset = 3;
                            }
                            else if (cands.contains(i) && cands.contains(j)) {
                                subset = 0;
                            }
                            else if (cands.contains(i) && cands.contains(k)) {
                                subset = 1;
                            }
                            else if (cands.contains(j) && cands.contains(k)){
                                subset = 2;
                            }
                            else {
                                subset = -1;
                            }
                            if (subset != -1) {
                                triples[i][j][k].addRowCell(row, cell, subset);
                                triples[i][j][k].addColCell(col, cell, subset);
                                triples[i][j][k].addRegCell(reg, cell, subset);
                            }
                        }
                    }
                }
            }
        }
        Vector<CandsPattern> hiddenTriples = new Vector<CandsPattern>();
        for (int num1 = 0; num1 < 9; num1++) {
            for (int num2 = num1 + 1; num2 < 9; num2++) {
                for (int num3 = num2 + 1; num3 < 9; num3++) {
                    int[] nums = new int[3];
                    nums[0] = num1;
                    nums[1] = num2;
                    nums[2] = num3;
                    for (int area = 1; area < 9; area++) {
                        Vector<Integer> rowCells = triples[num1][num2][num3].getRowCells(area);
                        Vector<Integer> colCells = triples[num1][num2][num3].getColCells(area);
                        Vector<Integer> regCells = triples[num1][num2][num3].getRegCells(area);
                        if (rowCells.size() == 3) {
                            CandsPattern hiddenTriple = new CandsPattern(TypeConstants.H_TRIPLE, new AreaStruct(AreaStruct.ROW, area), nums, rowCells);
                            boolean valid = true;
                            Vector<Integer> areaCells = hiddenTriple.getArea().getCells();
                            for (int i = 0; i < areaCells.size(); i++) {
                                // each member of the triple must not occur outside of the three cells that contain the triple
                                if (!rowCells.contains(areaCells.elementAt(i))) {
                                    Vector<Integer> cands = candidates.elementAt(areaCells.elementAt(i));
                                    if (cands.contains(num1) || cands.contains(num2) || cands.contains(num3)) {
                                        valid = false;
                                    }
                                }
                            }
                            if (valid && applyHiddenTuple(hiddenTriple, candidates, modify)) {
                                hiddenTriples.add(hiddenTriple);
                            }
                        }
                        if (colCells.size() == 3) {
                            CandsPattern hiddenTriple = new CandsPattern(TypeConstants.H_TRIPLE, new AreaStruct(AreaStruct.COL, area), nums, colCells);
                            boolean valid = true;
                            Vector<Integer> areaCells = hiddenTriple.getArea().getCells();
                            for (int i = 0; i < areaCells.size(); i++) {
                                if (!colCells.contains(areaCells.elementAt(i))) {
                                    Vector<Integer> cands = candidates.elementAt(areaCells.elementAt(i));
                                    if (cands.contains(num1) || cands.contains(num2) || cands.contains(num3)) {
                                        valid = false;
                                    }
                                }
                            }
                            if (valid && applyHiddenTuple(hiddenTriple, candidates, modify)) {
                                hiddenTriples.add(hiddenTriple);
                            }
                        }
                        if (regCells.size() == 3) {
                            CandsPattern hiddenTriple = new CandsPattern(TypeConstants.H_TRIPLE, new AreaStruct(AreaStruct.REG, area), nums, regCells);
                            boolean valid = true;
                            Vector<Integer> areaCells = hiddenTriple.getArea().getCells();
                            for (int i = 0; i < areaCells.size(); i++) {
                                if (!regCells.contains(areaCells.elementAt(i))) {
                                    Vector<Integer> cands = candidates.elementAt(areaCells.elementAt(i));
                                    if (cands.contains(num1) || cands.contains(num2) || cands.contains(num3)) {
                                        valid = false;
                                    }
                                }
                            }
                            if (valid && applyHiddenTuple(hiddenTriple, candidates, modify)) {
                                hiddenTriples.add(hiddenTriple);
                            }
                        }
                    }
                }
            }
        }
        return hiddenTriples;
    }

    /**
     *
     * @param hiddenTuple
     * @param candidates
     * @param modify
     * @return true if candidates were changed, false otherwise
     */
    public static boolean applyHiddenTuple(CandsPattern hiddenTuple, Vector<Vector<Integer>> candidates, boolean modify) {
        boolean simplifies = false;
        Vector<Integer> tupleCells = hiddenTuple.getCells();
        int[] nums = hiddenTuple.getNums();
        for (int i = 0; i < tupleCells.size(); i++) {
            Vector<Integer> cands = candidates.elementAt(tupleCells.elementAt(i));
            for (int j = 0; j < 9; j++) {
                if (cands.contains(j)) {
                    boolean remove = true;
                    for (int n = 0; n < nums.length; n++) {
                        if (j == nums[n]) {
                            remove = false;
                        }
                    }
                    if (remove) {
                        simplifies = true;
                        if (modify) {
                            cands.remove(new Integer(j));
                        }
                    }
                }

            }
        }
        return simplifies;
    }

    /**
     *
     * @param nakedTuple
     * @param candidates
     * @param modify
     * @return true if the candidates were changed, false otherwise
     */
    public static boolean applyNakedTuple(CandsPattern nakedTuple, Vector<Vector<Integer>> candidates, boolean modify) {
        boolean simplifies = false;
        Vector<Integer> areaCells = nakedTuple.getArea().getCells();
        Vector<Integer> tupleCells = nakedTuple.getCells();
        int[] nums = nakedTuple.getNums();
        for (int i = 0; i < areaCells.size(); i++) {
            int cell = areaCells.elementAt(i);
            if (!tupleCells.contains(cell)) {
                Vector<Integer> cands = candidates.elementAt(cell);
                if (nakedTuple.getType() == TypeConstants.N_PAIR) {
                    if (cands.contains(nums[0]) || cands.contains(nums[1])) {
                        simplifies = true;
                        if (modify) {
                            cands.remove(new Integer(nums[0]));
                            cands.remove(new Integer(nums[1]));
                        }
                    }
                }
                else if (nakedTuple.getType() == TypeConstants.N_TRIPLE) {
                    if (cands.contains(nums[0]) || cands.contains(nums[1]) || cands.contains(nums[2])) {
                        simplifies = true;
                        if (modify) {
                            cands.remove(new Integer(nums[0]));
                            cands.remove(new Integer(nums[1]));
                            cands.remove(new Integer(nums[2]));
                        }
                    }
                }
            }
        }
        return simplifies;
    }

    private static class SudokuPair {
        private int num1, num2;
        private Vector<Vector<Integer>> rowCells;
        private Vector<Vector<Integer>> colCells;
        private Vector<Vector<Integer>> regCells;
        private Vector<Integer> containerCells;

        public SudokuPair(int n1, int n2) {
            num1 = n1;
            num2 = n2;
            rowCells = new Vector<Vector<Integer>>();
            colCells = new Vector<Vector<Integer>>();
            regCells = new Vector<Vector<Integer>>();
            for (int i = 0; i < 9; i++) {
                rowCells.add(new Vector<Integer>());
                colCells.add(new Vector<Integer>());
                regCells.add(new Vector<Integer>());
            }
            containerCells = new Vector<Integer>();
        }

        public void addRowCell(int row, int cell) {
            rowCells.elementAt(row).add(cell);
        }
        public void addColCell(int col, int cell) {
            colCells.elementAt(col).add(cell);
        }
        public void addRegCell(int reg, int cell) {
            regCells.elementAt(reg).add(cell);
        }
        public void addContainerCell(int cell) {
            containerCells.add(cell);
        }
        public Vector<Integer> getRowCells(int row) {
            return rowCells.elementAt(row);
        }
        public Vector<Integer> getColCells(int col) {
            return colCells.elementAt(col);
        }
        public Vector<Integer> getRegCells(int reg) {
            return regCells.elementAt(reg);
        }
        public Vector<Integer> getContainerCells() {
            return containerCells;
        }
        public boolean contains(int num) {
            return (num1 == num || num2 == num);
        }

        public int[] toValuesArray() {
            int arr[] = new int[2];
            arr[0] = num1;
            arr[1] = num2;
            return arr;
        }

        public boolean equals(SudokuPair p) {
            return (p.contains(num1) && p.contains(num2));
        }
    }

    /*
     * Represents a triple along with the the cells in which it occurs. Two out of the three numbers is sufficient
     * for an occurrence
     */
    private static class SudokuTriple {
        private int num1, num2, num3;

        private HashMap<Integer, Vector<Vector<Integer>>> subsetRowCells;
        private HashMap<Integer, Vector<Vector<Integer>>> subsetColCells;
        private HashMap<Integer, Vector<Vector<Integer>>> subsetRegCells;

        public SudokuTriple(int n1, int n2, int n3) {
            num1 = n1;
            num2 = n2;
            num3 = n3;

            subsetRowCells = new HashMap<Integer, Vector<Vector<Integer>>>();
            subsetColCells = new HashMap<Integer, Vector<Vector<Integer>>>();
            subsetRegCells = new HashMap<Integer, Vector<Vector<Integer>>>();

            for (int i = 0; i < 4; i++) {
                subsetRowCells.put(i, new Vector<Vector<Integer>>());
                subsetColCells.put(i, new Vector<Vector<Integer>>());
                subsetRegCells.put(i, new Vector<Vector<Integer>>());
            }

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 4; j++) {
                    subsetRowCells.get(j).add(new Vector<Integer>());
                    subsetColCells.get(j).add(new Vector<Integer>());
                    subsetRegCells.get(j).add(new Vector<Integer>());
                }
            }
        }


        public void addRowCell(int row, int cell, int subset) {
            subsetRowCells.get(subset).elementAt(row).add(cell);
        }
        public void addColCell(int col, int cell, int subset) {
            subsetColCells.get(subset).elementAt(col).add(cell);
        }
        public void addRegCell(int reg, int cell, int subset) {
            subsetRegCells.get(subset).elementAt(reg).add(cell);
        }

        public Vector<Integer> getRowCells(int row) {
            Vector<Integer> rowCells = new Vector<Integer>();
            for (int i = 0; i < subsetRowCells.get(3).elementAt(row).size(); i++) {
                rowCells.add(subsetRowCells.get(3).elementAt(row).elementAt(i));
            }
            for (int sub = 0; sub < 3; sub++) {
                // ignore naked pairs
                if (subsetRowCells.get(sub).elementAt(row).size() == 1) {
                    rowCells.add(subsetRowCells.get(sub).elementAt(row).firstElement());
                }
            }
            return rowCells;
        }
        public Vector<Integer> getColCells(int col) {
            Vector<Integer> colCells = new Vector<Integer>();
            for (int i = 0; i < subsetColCells.get(3).elementAt(col).size(); i++) {
                colCells.add(subsetColCells.get(3).elementAt(col).elementAt(i));
            }
            for (int sub = 0; sub < 3; sub++) {
                if (subsetColCells.get(sub).elementAt(col).size() == 1) {
                    colCells.add(subsetColCells.get(sub).elementAt(col).firstElement());
                }
            }
            return colCells;
        }
        public Vector<Integer> getRegCells(int reg) {
            Vector<Integer> regCells = new Vector<Integer>();
            for (int i = 0; i < subsetRegCells.get(3).elementAt(reg).size(); i++) {
                regCells.add(subsetRegCells.get(3).elementAt(reg).elementAt(i));
            }
            for (int sub = 0; sub < 3; sub++) {
                if (subsetRegCells.get(sub).elementAt(reg).size() == 1) {
                    regCells.add(subsetRegCells.get(sub).elementAt(reg).firstElement());
                }
            }
            return regCells;
        }

        public boolean contains(int num) {
            return (num1 == num || num2 == num || num3 == num);
        }

        public int[] toValuesArray() {
            int[] arr = new int[3];
            arr[0] = num1;
            arr[1] = num2;
            arr[2] = num3;
            return arr;
        }
    }
}
