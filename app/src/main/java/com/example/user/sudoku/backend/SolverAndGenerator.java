package com.example.user.sudoku.backend;

import java.util.Date;

import java.util.Random;
import java.util.Vector;


import com.example.user.sudoku.backend.heuristics.HeuristicsSolvers;

/*
 * Stephen Rojcewicz
 *
 * Contains the main solution and puzzle generation algorithms
 *
 */

public class SolverAndGenerator {

    public static boolean findSolution(SudokuGraph board) {
        if (!board.hasValidConfiguration()) {
            return false;
        }
        return solve(board, 0);
    }

    /**
     * Tries candidates in a random order to create a random solution, if board has more than one solution.
     * Used by the puzzle generator to randomly solve a blank board
     * @param board
     * @param rand
     * @return
     */
    public static boolean findSolutionRandom(SudokuGraph board, Random rand) {
        if (!board.hasValidConfiguration()) {
            return false;
        }
        return solveRandom(board, 0, rand);
    }

    public static Vector<Vector<Integer>> makePuzzle(int difficulty) {
        Random rand = new Random(new Date().getTime());
        int numNodesToRemove;
        switch (difficulty) {
            case TypeConstants.EASY:
                numNodesToRemove = 50 + rand.nextInt(4);
                break;
            case TypeConstants.MEDIUM:
                numNodesToRemove = 53 + rand.nextInt(4);
                break;
            case TypeConstants.HARD:
                numNodesToRemove = 56;
                break;
            default: numNodesToRemove = 55 + rand.nextInt(3);
        }


        int[] emptyPuzzle = new int[81];
        for (int i = 0; i < 81; i++) {
            emptyPuzzle[i] = TypeConstants.BLANK;
        }
        SudokuGraph board = new SudokuGraph(emptyPuzzle);
        boolean solvable = findSolutionRandom(board, rand);
        SudokuGraph puzzle = new SudokuGraph(board.valuesArray());
        boolean success = removeNodes(puzzle, numNodesToRemove);
        boolean satisfiesDifficulty = false;
        if (success) {
            switch(difficulty) {
                case TypeConstants.EASY: satisfiesDifficulty = HeuristicsSolvers.solveWithNakedSingles(puzzle.findAllCandidates(), 0);
                    break;
                case TypeConstants.MEDIUM: satisfiesDifficulty = (HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates()) && !HeuristicsSolvers.solveWithNakedSingles(puzzle.findAllCandidates(), 0));
                    break;
                case TypeConstants.HARD: satisfiesDifficulty = (HeuristicsSolvers.solveWithHiddenTriples(puzzle.findAllCandidates()) && !HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates()));
                    break;
                default: satisfiesDifficulty = !HeuristicsSolvers.solveWithHiddenTriples(puzzle.findAllCandidates());
            }
        }
        int attempts = 1;
        int maxAttempts = 20;
        while (!(success && satisfiesDifficulty) && attempts < maxAttempts) {
            board = null;
            puzzle = null;
            board = new SudokuGraph(emptyPuzzle);
            solvable = findSolutionRandom(board, rand);
            puzzle = new SudokuGraph(board.valuesArray());
            success = removeNodes(puzzle, numNodesToRemove);
            if (success) {
                switch(difficulty) {
                    case TypeConstants.EASY: satisfiesDifficulty = HeuristicsSolvers.solveWithNakedSingles(puzzle.findAllCandidates(), 0);
                        break;
                    case TypeConstants.MEDIUM: satisfiesDifficulty = (HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates()) && !HeuristicsSolvers.solveWithNakedSingles(puzzle.findAllCandidates(), 0));
                        break;
                    case TypeConstants.HARD: satisfiesDifficulty = (HeuristicsSolvers.solveWithHiddenTriples(puzzle.findAllCandidates()) && !HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates()));
                        break;
                    default: satisfiesDifficulty = !HeuristicsSolvers.solveWithHiddenTriples(puzzle.findAllCandidates());
                }
            }
            attempts++;
        }
        if (!(success && satisfiesDifficulty)) {
            return null;
        }
        System.out.println(attempts + " attempts");
        return puzzle.committedNumberes();
    }

    /**
     * Randomly generates a puzzle with the specified number of blanks
     * @param numNodesToRemove
     * @return a SudokuGraph representation of the puzzle
     */
    public static SudokuGraph makePuzzleGraph(int numNodesToRemove) {
        int[] emptyPuzzle = new int[81];
        Random rand = new Random(new Date().getTime());
        for (int i = 0; i < 81; i++) {
            emptyPuzzle[i] = TypeConstants.BLANK;
        }
        SudokuGraph board = new SudokuGraph(emptyPuzzle);
        boolean solvable = findSolutionRandom(board, rand);
        SudokuGraph puzzle = new SudokuGraph(board.valuesArray());
        boolean success = removeNodes(puzzle, numNodesToRemove);
        int attempts = 1;
        int maxAttempts = 35;
        while (!success && attempts < maxAttempts) {
            board = null;
            puzzle = null;
            board = new SudokuGraph(emptyPuzzle);
            solvable = findSolutionRandom(board, rand);
            puzzle = new SudokuGraph(board.valuesArray());
            success = removeNodes(puzzle, numNodesToRemove);
            attempts++;
        }
        if (!success) {
            return null;
        }
        return puzzle;
    }

    /**
     * board is assumed to have a valid configuration at all nodes except prevNode. No assumption is made about prevNode.
     */
    private static boolean solve(SudokuGraph board, int prevNode) {

        if (!board.isValidAt(prevNode)) {
            return false;
        } else {
            if (!board.hasBlankNodes()) {
                return true;
            }

            boolean solvable = false;
            int candidate = 0;
            int node = board.dequeueBlankNode();
            board.putValueAt(node, candidate);
            while (!(solvable = solve(board, node)) && candidate < 8) {
                board.putValueAt(node, ++candidate);
            }
            if (!solvable) {
                board.putValueAt(node, TypeConstants.BLANK); // restore old value since none of the trials were successful; automatically enqueues node
            }
            return solvable;
        }
    }

    private static boolean solveRandom(SudokuGraph board, int prevNode, Random rand) {

        if (!board.isValidAt(prevNode)) {
            return false;
        } else {
            if (!board.hasBlankNodes()) {
                return true;
            }

            boolean solvable = false;
            int node = board.dequeueBlankNode();
            Vector<Integer> candidates = new Vector<Integer>();

            for (int i = 0; i < 9; i++) {
                candidates.add(i);
            }

            Integer candidate = candidates.elementAt(rand.nextInt(candidates.size()));
            candidates.remove(candidate);

            board.putValueAt(node, candidate);
            while (!((solvable = solveRandom(board, node, rand)) || candidates.isEmpty())) {
                candidate = candidates.elementAt(rand.nextInt(candidates.size()));
                candidates.remove(candidate);
                board.putValueAt(node, candidate);
            }
            if (!solvable) {
                board.putValueAt(node, TypeConstants.BLANK); // restore old value since none of the trials were successful; automatically enqueues node
            }
            return solvable;
        }
    }

    private static boolean removeNodes(SudokuGraph board, int numNodesToRemove) {
        int node;
        int removedCount = 0;
        while (numNodesToRemove > removedCount && board.hasAssignedNodes()) {
            node = board.dequeueAssignedNode();
            if (canRemove(board, node)) {
                board.putValueAt(node, TypeConstants.BLANK);
                removedCount++;
            }
        }
        return (numNodesToRemove <= removedCount);
    }


    /**
     * Does not modify board
     * @param board
     * @param node
     * @return
     */
    private static boolean canRemove(SudokuGraph board, int node) {
        int oldValue = board.getValueAt(node);
        int candidate = (oldValue == 0)? 1: 0;
        boolean solvable = false;
        board.putValueAt(node, candidate);


        if (oldValue == 8) {
            while (!(solvable = testSolvable(board, node)) && candidate < 7) {
                board.putValueAt(node, ++candidate);
            }
        }
        else {
            while (!(solvable = testSolvable(board, node)) && candidate < 8) {
                candidate = ((candidate + 1) == oldValue )? candidate + 2 : candidate + 1;
                board.putValueAt(node, candidate);
            }
        }
        board.putValueAt(node, oldValue);
        return (!solvable);
    }

    /**
     * Does not modify board
     * @param board
     * @param prevNode
     * @return
     */
    private static boolean testSolvable(SudokuGraph board, int prevNode) {

        if (!board.isValidAt(prevNode)) {
            return false;
        } else {
            if (!board.hasBlankNodes()) {
                return true;
            }

            boolean solvable = false;
            int candidate = 0;
            int node = board.dequeueBlankNode();
            board.putValueAt(node, candidate);
            while (!(solvable = testSolvable(board, node)) && candidate < 8) {
                board.putValueAt(node, ++candidate);
            }
            board.putValueAt(node, TypeConstants.BLANK); // restore old value so that board is not modified
            return solvable;
        }
    }

    public static boolean testSingleSolution(SudokuGraph board) {
        int[] boardArray = board.valuesArray();
        board = null;

        Vector<Integer> blanks = new Vector<Integer>();

        // get the blank nodes
        for (int i = 0; i < 81; i++) {
            if (boardArray[i] == TypeConstants.BLANK) {
                blanks.add(i);
            }
        }

        boolean hasSingleSolution = true;
        for (int i = 0; i < blanks.size(); i++) {
            int node = blanks.elementAt(i);
            int solvableCount = 0;
            for (int candidate = 0; candidate < 9; candidate++) {
                boardArray[node] = candidate;
                if (testSolvable(new SudokuGraph(boardArray), node)) {
                    solvableCount++;
                }
            }
            boardArray[node] = TypeConstants.BLANK;
            if (solvableCount > 1) {
                hasSingleSolution = false;
            }
        }
        return hasSingleSolution;
    }

}
