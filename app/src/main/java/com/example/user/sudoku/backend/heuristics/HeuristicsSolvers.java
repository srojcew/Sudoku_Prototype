package com.example.user.sudoku.backend.heuristics;

import java.util.Vector;

import com.example.user.sudoku.backend.SudokuGraph;
import com.example.user.sudoku.backend.TypeConstants;

/*
 * Stephen Rojcewicz
 *
 * Contains the heuristics solvers used by the puzzle generator to determine the difficulty of a puzzle.
 *
 */

public class HeuristicsSolvers {

    public static boolean solveWithNakedSingles(Vector<Vector<Integer>> candidates, int prevSolvedNodesCount) {
        int solvedNodesCount = 0;
        for (int i = 0; i < 81; i++) {
            if (candidates.elementAt(i).size() == 1) {
                solvedNodesCount++;
            }
            else if (candidates.elementAt(i).isEmpty()) {
                return false;
            }
        }
        if (solvedNodesCount < prevSolvedNodesCount) {
            return false;
        }
        if (solvedNodesCount == 81) {
            return true;
        }
        if (solvedNodesCount == prevSolvedNodesCount) {
            return false;
        }
        return solveWithNakedSingles(new SudokuGraph(candidates).findAllCandidates(), solvedNodesCount);
    }

    /**
     * Also uses naked singles
     * @param candidates
     * @return
     */
    public static boolean solveWithHiddenSingles(Vector<Vector<Integer>> candidates) {
        SudokuGraph puzzle = new SudokuGraph(candidates);
        Hint hint = HintCreator.createSolverHint(puzzle.findAllCandidates(), puzzle.valuesArray());
        while (hint != null && (hint.getPattern().getType() == TypeConstants.H_SINGLE || hint.getPattern().getType() == TypeConstants.N_SINGLE)) {
            CandsPattern pattern = hint.getPattern();
            puzzle.putValueAt(pattern.getCells().firstElement(), pattern.getNums()[0]);
            hint = HintCreator.createSolverHint(puzzle.findAllCandidates(), puzzle.valuesArray());
        }
        if (countSolvedNodes(puzzle.findAllCandidates()) == 81) {
            return true;
        }
        return false;
    }

    /**
     * Also uses singles
     * @param candidates
     * @return
     */
    public static boolean solveWithNakedPairs(Vector<Vector<Integer>> candidates) {
        SudokuGraph puzzle = new SudokuGraph(candidates);
        candidates = puzzle.findAllCandidates();
        Hint hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        while (hint != null) {
            CandsPattern pattern = hint.getPattern();
            if (pattern.getType() == TypeConstants.N_SINGLE || pattern.getType() == TypeConstants.H_SINGLE) {
                puzzle.putValueAt(pattern.getCells().firstElement(), pattern.getNums()[0]);
            }
            else if (pattern.getType() == TypeConstants.N_PAIR) {
                // update the candidates according to pattern
                if(!HeuristicsAnalyzer.applyNakedTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else {
                break;
            }
            candidates = puzzle.updateAllCandidates(candidates);
            hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        }
        if (countSolvedNodes(puzzle.findAllCandidates()) == 81) {
            return true;
        }
        return false;
    }

    /**
     * Also uses naked pairs and singles
     * @param candidates
     * @return
     */
    public static boolean solveWithNakedTriples(Vector<Vector<Integer>> candidates) {
        SudokuGraph puzzle = new SudokuGraph(candidates);
        candidates = puzzle.findAllCandidates();
        Hint hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        while (hint != null) {
            CandsPattern pattern = hint.getPattern();
            if (pattern.getType() == TypeConstants.N_SINGLE || pattern.getType() == TypeConstants.H_SINGLE) {
                puzzle.putValueAt(pattern.getCells().firstElement(), pattern.getNums()[0]);
            }
            else if (pattern.getType() == TypeConstants.N_PAIR || pattern.getType() == TypeConstants.N_TRIPLE) {
                // update the candidates according to pattern
                if (!HeuristicsAnalyzer.applyNakedTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else {
                break;
            }
            candidates = puzzle.updateAllCandidates(candidates);
            hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        }
        if (countSolvedNodes(puzzle.findAllCandidates()) == 81) {
            return true;
        }
        return false;
    }

    /**
     * Also uses naked tuples and singles
     * @param candidates
     * @return
     */
    public static boolean solveWithHiddenPairs(Vector<Vector<Integer>> candidates) {
        SudokuGraph puzzle = new SudokuGraph(candidates);
        candidates = puzzle.findAllCandidates();
        Hint hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        while (hint != null) {
            CandsPattern pattern = hint.getPattern();
            if (pattern.getType() == TypeConstants.N_SINGLE || pattern.getType() == TypeConstants.H_SINGLE) {
                puzzle.putValueAt(pattern.getCells().firstElement(), pattern.getNums()[0]);
            }
            else if (pattern.getType() == TypeConstants.N_PAIR || pattern.getType() == TypeConstants.N_TRIPLE) {
                // update the candidates according to pattern
                if (!HeuristicsAnalyzer.applyNakedTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else if (pattern.getType() == TypeConstants.H_PAIR) {
                // update the candidates according to pattern
                if (!HeuristicsAnalyzer.applyHiddenTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else {
                break;
            }
            candidates = puzzle.updateAllCandidates(candidates);
            hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        }
        if (countSolvedNodes(puzzle.findAllCandidates()) == 81) {
            return true;
        }
        return false;
    }

    /**
     * Also uses naked tuples, hidden pairs, and singles
     * @param candidates
     * @return
     */
    public static boolean solveWithHiddenTriples(Vector<Vector<Integer>> candidates) {
        SudokuGraph puzzle = new SudokuGraph(candidates);
        candidates = puzzle.findAllCandidates();
        Hint hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        while (hint != null) {
            CandsPattern pattern = hint.getPattern();
            if (pattern.getType() == TypeConstants.N_SINGLE || pattern.getType() == TypeConstants.H_SINGLE) {
                puzzle.putValueAt(pattern.getCells().firstElement(), pattern.getNums()[0]);
            }
            else if (pattern.getType() == TypeConstants.N_PAIR || pattern.getType() == TypeConstants.N_TRIPLE) {
                // update the candidates according to pattern
                if (!HeuristicsAnalyzer.applyNakedTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else if (pattern.getType() == TypeConstants.H_PAIR || pattern.getType() == TypeConstants.H_TRIPLE) {
                // update the candidates according to pattern
                if (!HeuristicsAnalyzer.applyHiddenTuple(pattern, candidates, true)) {
                    System.err.println("should simplify");
                }
                puzzle = null;
                puzzle = new SudokuGraph(candidates);
            }
            else {
                System.err.println("invalid type in solveWithHiddenTriples()");
                System.exit(1);
            }
            candidates = puzzle.updateAllCandidates(candidates);
            hint = HintCreator.createSolverHint(candidates, puzzle.valuesArray());
        }
        if (countSolvedNodes(puzzle.findAllCandidates()) == 81) {
            return true;
        }
        return false;
    }

    private static int countSolvedNodes(Vector<Vector<Integer>> candidates) {
        int solvedCount = 0;
        for (int i = 0; i < 81; i++) {
            if (candidates.elementAt(i).size() == 1) {
                solvedCount++;
            }
        }
        return solvedCount;
    }

}
