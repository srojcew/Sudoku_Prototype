package com.example.user.sudoku.backend.heuristics;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import com.example.user.sudoku.backend.HintResponse;
import com.example.user.sudoku.backend.SolverAndGenerator;
import com.example.user.sudoku.backend.SudokuGraph;
import com.example.user.sudoku.backend.TypeConstants;

/*
 * Stephen Rojcewicz
 *
 * The main hint engine.
 *
 */

public class HintCreator {
    public static final String invalidCandStr = "The puzzle cannot be solved with the candidates you entered in the highlighted cell.";

    /**
     * Creates a hint for the user in GameMode
     * @param userCandidates
     * @param committedNumbers
     * @return
     */
    public static Hint createUserHint(Vector<Vector<Integer>> userCandidates, int[] committedNumbers) {
        // first attempt to find a general hint
        Hint generalHint = createGeneralHint(userCandidates, committedNumbers);
        if (generalHint != null) {
            return generalHint;
        }
        Random rand = new Random(new Date().getTime());
        Vector<Vector<Integer>> candidates = new SudokuGraph(committedNumbers).updateAllCandidatesBlanks(userCandidates);

        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(candidates, committedNumbers);
        if (!nakedSingles.isEmpty()) {
            CandsPattern nakedSingle = nakedSingles.elementAt(rand.nextInt(nakedSingles.size()));
            if (userCandidates.elementAt(nakedSingle.getCells().firstElement()).size() > 1) {
                return new Hint("Your candidates do not reflect the current board. Click Update Candidates or modify them yourself.");
            }
            else {
                return new Hint(nakedSingle);
            }
        }

        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(candidates);
        if (!hiddenSingles.isEmpty()) {
            return new Hint(hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size())));
        }

        // find patterns for the user's candidates and for the actual calculated candidates
        Vector<CandsPattern> userNakedPairs = HeuristicsAnalyzer.findNakedPairs(userCandidates, false);
        Vector<CandsPattern> userNakedTriples = HeuristicsAnalyzer.findNakedTriples(userCandidates, false);
        Vector<CandsPattern> userHiddenPairs = HeuristicsAnalyzer.findHiddenPairs(userCandidates, false);
        Vector<CandsPattern> userHiddenTriples = HeuristicsAnalyzer.findHiddenTriples(userCandidates, false);

        Vector<CandsPattern> nakedPairs = HeuristicsAnalyzer.findNakedPairs(candidates, false);
        Vector<CandsPattern> nakedTriples = HeuristicsAnalyzer.findNakedTriples(candidates, false);
        Vector<CandsPattern> hiddenPairs = HeuristicsAnalyzer.findHiddenPairs(candidates, false);
        Vector<CandsPattern> hiddenTriples = HeuristicsAnalyzer.findHiddenTriples(candidates, false);

        if ((userNakedPairs.isEmpty() && userNakedTriples.isEmpty() && userHiddenPairs.isEmpty() && userHiddenTriples.isEmpty())) {
            if (!(nakedPairs.isEmpty() && nakedTriples.isEmpty() && hiddenPairs.isEmpty() && hiddenTriples.isEmpty())) {

                // check if the user has entered all of the candidates
                for (int i = 0; i < 81; i++) {
                    if (userCandidates.elementAt(i).isEmpty()) {
                        return new Hint("A strategy that requires candidates is needed. Enter all of the candidates or click Update Candidates");
                    }
                }
                // check if the user's candidates are up to date
                for (int i = 0; i < 81; i++) {
                    for (int j = 0; j < userCandidates.size(); j++) {
                        if (!candidates.elementAt(i).contains(userCandidates.elementAt(i).elementAt(j))) {
                            return new Hint("Your candidates do not reflect the current board. Click Update Candidates or modify them yourself.");
                        }
                    }
                }
                System.err.println("user's candidates should agree with actual candidats");
                System.exit(1);
                return null;
            }
            else {
                return new Hint("Make a guess.");
            }
        }

        else {
            if (!userNakedPairs.isEmpty()) {
                return new Hint(userNakedPairs.elementAt(rand.nextInt(userNakedPairs.size())));
            }

            if (!userNakedTriples.isEmpty()) {
                return new Hint(userNakedTriples.elementAt(rand.nextInt(userNakedTriples.size())));
            }

            if (!userHiddenPairs.isEmpty()) {
                return new Hint(userHiddenPairs.elementAt(rand.nextInt(userHiddenPairs.size())));
            }

            if (!userHiddenTriples.isEmpty()) {
                return new Hint(userHiddenTriples.elementAt(rand.nextInt(userHiddenTriples.size())));
            }
        }
        // code should not be reachable
        System.err.println("see HintCreator.java");
        System.exit(1);
        return null;
    }

    /**
     * Creates a hint to be used by the heuristics solvers
     * @param candidates
     * @param committedNumbers
     * @return
     */
    public static Hint createSolverHint(Vector<Vector<Integer>> candidates, int[] committedNumbers) {
        Random rand = new Random(new Date().getTime());

        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(candidates, committedNumbers);
        if (!nakedSingles.isEmpty()) {
            return new Hint(nakedSingles.elementAt(rand.nextInt(nakedSingles.size())));
        }

        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(candidates);
        if (!hiddenSingles.isEmpty()) {
            return new Hint(hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size())));
        }
        Vector<CandsPattern> nakedPairs = HeuristicsAnalyzer.findNakedPairs(candidates, false);
        if (!nakedPairs.isEmpty()) {
            return new Hint(nakedPairs.elementAt(rand.nextInt(nakedPairs.size())));
        }

        Vector<CandsPattern> nakedTriples = HeuristicsAnalyzer.findNakedTriples(candidates, false);
        if (!nakedTriples.isEmpty()) {
            return new Hint(nakedTriples.elementAt(rand.nextInt(nakedTriples.size())));
        }
        Vector<CandsPattern> hiddenPairs = HeuristicsAnalyzer.findHiddenPairs(candidates, false);
        if (!hiddenPairs.isEmpty()) {
            return new Hint(hiddenPairs.elementAt(rand.nextInt(hiddenPairs.size())));
        }
        Vector<CandsPattern> hiddenTriples = HeuristicsAnalyzer.findHiddenTriples(candidates, false);
        if (!hiddenTriples.isEmpty()) {
            return new Hint(hiddenTriples.elementAt(rand.nextInt(hiddenTriples.size())));
        }

        return null;
    }


    public static Hint createHintAt(int row, int col, Vector<Vector<Integer>> userCandidates, int[] committedNumbers) {
        if (committedNumbers[9 * row + col] != TypeConstants.BLANK) {
            return new Hint("You already have a number in that cell");
        }
        // attempt to find a general hint
        Hint generalHint = createGeneralHint(userCandidates, committedNumbers);
        if (generalHint != null) {
            return generalHint;
        }
        Random rand = new Random(new Date().getTime());
        Vector<Vector<Integer>> candidates = new SudokuGraph(committedNumbers).updateAllCandidatesBlanks(userCandidates);

        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(candidates, committedNumbers);
        if (!nakedSingles.isEmpty()) {
            for (int i = 0; i < nakedSingles.size(); i++) {
                CandsPattern nakedSingle = nakedSingles.elementAt(i);
                if (nakedSingle.getCells().firstElement().equals(9 * row + col)) {
                    if (userCandidates.elementAt(9 * row + col).size() > 1) {
                        return new Hint("Your candidates for that cell do not reflect the current board. Click Update Candidates or modify them yourself.");
                    }
                    else {
                        return new Hint(nakedSingle);
                    }
                }
            }
        }

        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(candidates);
        for (int i = 0; i < hiddenSingles.size(); i++) {
            if (hiddenSingles.elementAt(i).getCells().firstElement().equals(9 * row + col) || hiddenSingles.elementAt(i).getArea().getCells().contains(9 * row + col)) {
                return new Hint(hiddenSingles.elementAt(i));
            }
        }

        // find patterns for the user's candidates and for the actual calculated candidates
        Vector<CandsPattern> userNakedPairs = HeuristicsAnalyzer.findNakedPairs(userCandidates, false);
        Vector<CandsPattern> userNakedTriples = HeuristicsAnalyzer.findNakedTriples(userCandidates, false);
        Vector<CandsPattern> userHiddenPairs = HeuristicsAnalyzer.findHiddenPairs(userCandidates, false);
        Vector<CandsPattern> userHiddenTriples = HeuristicsAnalyzer.findHiddenTriples(userCandidates, false);

        Vector<CandsPattern> nakedPairs = HeuristicsAnalyzer.findNakedPairs(candidates, false);
        Vector<CandsPattern> nakedTriples = HeuristicsAnalyzer.findNakedTriples(candidates, false);
        Vector<CandsPattern> hiddenPairs = HeuristicsAnalyzer.findHiddenPairs(candidates, false);
        Vector<CandsPattern> hiddenTriples = HeuristicsAnalyzer.findHiddenTriples(candidates, false);

        // discard patterns that do not affect the requested cell
        Vector<Vector<CandsPattern>> patternsList = new Vector<Vector<CandsPattern>>();
        patternsList.add(userNakedPairs);
        patternsList.add(userNakedTriples);
        patternsList.add(userHiddenPairs);
        patternsList.add(userHiddenTriples);
        patternsList.add(nakedPairs);
        patternsList.add(nakedTriples);
        patternsList.add(hiddenPairs);
        patternsList.add(hiddenTriples);
        for (int i = 0; i < patternsList.size(); i++) {
            Vector<CandsPattern> patterns = patternsList.elementAt(i);
            for (int j = 0; j < patterns.size(); j++) {
                if (!patterns.elementAt(j).getCells().contains(9 * row + col)) {
                    patterns.removeElementAt(j);
                    j--;
                }
            }
        }

        if ((userNakedPairs.isEmpty() && userNakedTriples.isEmpty() && userHiddenPairs.isEmpty() && userHiddenTriples.isEmpty())) {
            if (!(nakedPairs.isEmpty() && nakedTriples.isEmpty() && hiddenPairs.isEmpty() && hiddenTriples.isEmpty())) {

                // check if the user has entered all of the candidates
                for (int i = 0; i < 81; i++) {
                    if (userCandidates.elementAt(i).isEmpty()) {
                        return new Hint("A strategy that requires candidates is needed for that cell. Enter all of the candidates or click Update Candidates");
                    }
                }
                // check if the user's candidates are up to date
                for (int i = 0; i < 81; i++) {
                    for (int j = 0; j < userCandidates.size(); j++) {
                        if (!candidates.elementAt(i).contains(userCandidates.elementAt(i).elementAt(j))) {
                            return new Hint("Your candidates do not reflect the current board. Click Update Candidates or modify them yourself.");
                        }
                    }
                }
                System.err.println("user's candidates should agree with actual candidats");
                System.exit(1);
                return null;
            }
            else {
                return new Hint("There are no hints for that cell.");
            }
        }

        else {
            if (!userNakedPairs.isEmpty()) {
                return new Hint(userNakedPairs.elementAt(rand.nextInt(userNakedPairs.size())));
            }

            if (!userNakedTriples.isEmpty()) {
                return new Hint(userNakedTriples.elementAt(rand.nextInt(userNakedTriples.size())));
            }

            if (!userHiddenPairs.isEmpty()) {
                return new Hint(userHiddenPairs.elementAt(rand.nextInt(userHiddenPairs.size())));
            }

            if (!userHiddenTriples.isEmpty()) {
                return new Hint(userHiddenTriples.elementAt(rand.nextInt(userHiddenTriples.size())));
            }
        }
        // code should not be reachable
        System.err.println("see HintCreator.java");
        System.exit(1);
        return null;
    }

    /**
     * Creates a hint that does not apply to a specific heuristic. Returns null if no such hint exists
     * @param userCandidates
     * @param committedNumbers
     * @return
     */
    private static Hint createGeneralHint(Vector<Vector<Integer>> userCandidates, int[] committedNumbers) {
        SudokuGraph board = new SudokuGraph(committedNumbers);
        if (!SolverAndGenerator.findSolution(board)) {
            return new Hint("The puzzle is not solvable");
        }
        boolean alreadySolved = true;
        for (int i = 0; i < 81; i++) {
            // check if the user's candidate lists contain the correct candidate TODO: implement in SudokuGraph
            if (!(userCandidates.elementAt(i).isEmpty() || userCandidates.elementAt(i).contains(board.getValueAt(i)))) {
                Vector<Integer> cellsAffected = new Vector<Integer>();
                cellsAffected.add(i);
                return new Hint(invalidCandStr, cellsAffected, true);
            }
            // check if the puzzle is already solved
            if (committedNumbers[i] == TypeConstants.BLANK) {
                alreadySolved = false;
            }
        }
        if (alreadySolved) {
            return new Hint("The puzzle is solved.");
        }
        return null;
    }


    public static HintResponse applyHint(Hint hint, Vector<Vector<Integer>> candidates, int[] committedNums) {
        // invalid candidates hints do not have patterns, so check for them first
        if (hint.getMessage().equals(invalidCandStr)) {
            SudokuGraph puzzle = new SudokuGraph(committedNums);
            Vector<Integer> newCands = new SudokuGraph(committedNums).findCandidatesAt(hint.getAffectedCells().firstElement());
            boolean simplifies = false;
            int cell = hint.getAffectedCells().firstElement();
            if (SolverAndGenerator.findSolution(puzzle)) {
                if (!(candidates.elementAt(cell).isEmpty() || candidates.elementAt(cell).contains(puzzle.getValueAt(cell)))) {
                    simplifies = true;
                    candidates.set(cell, newCands);
                }
            }
            else {
                System.err.println("puzzle should be solvable in HintCreator");
                System.exit(1);
            }

            return new HintResponse(candidates, committedNums, simplifies);
        }

        CandsPattern pattern = hint.getPattern();
        boolean simplifies = false;
        if (pattern.getType() == TypeConstants.N_SINGLE || pattern.getType() == TypeConstants.H_SINGLE) {
            if (committedNums[pattern.getCells().firstElement()] != pattern.getNums()[0]) {
                simplifies = true;
                committedNums[pattern.getCells().firstElement()] = pattern.getNums()[0];
            }
            candidates.elementAt(pattern.getCells().firstElement()).clear();
            candidates.elementAt(pattern.getCells().firstElement()).add(pattern.getNums()[0]);
        }
        else if (pattern.getType() == TypeConstants.N_PAIR || pattern.getType() == TypeConstants.N_TRIPLE) {
            simplifies = HeuristicsAnalyzer.applyNakedTuple(pattern, candidates, true);
        }
        else if (pattern.getType() == TypeConstants.H_PAIR || pattern.getType() == TypeConstants.H_TRIPLE) {
            simplifies = HeuristicsAnalyzer.applyHiddenTuple(pattern, candidates, true);
        }
        else {
            System.err.println("invalid pattern type");
            System.exit(1);
        }
        return new HintResponse(candidates, committedNums, simplifies);
    }
}
