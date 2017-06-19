package com.example.user.sudoku.backend;

import java.util.Random;
import java.util.Vector;

import com.example.user.sudoku.backend.heuristics.Hint;
import com.example.user.sudoku.backend.heuristics.HintCreator;
import com.example.user.sudoku.backend.heuristics.lessons.Lesson;

public class Backend {
    /**
     *
     * @param stringBoard
     *            row major board representation. Blank squares are represented
     *            by integer TypeConstants.BLANK
     * @return
     */
    public String[] findSolution(String[] stringBoard) {
        int[] boardArray = boardStringToInt(stringBoard);
        SudokuGraph board = new SudokuGraph(boardArray);
        boolean solvable = SolverAndGenerator.findSolution(board);
        if (!solvable) {
            return null;
        } else {
            return boardIntToString(board.valuesArray());
        }
    }

    public String[][] makePuzzle(int difficulty) {
        Vector<Vector<Integer>> puzzle = SolverAndGenerator.makePuzzle(difficulty);
        if (puzzle == null) {
            return null;
        }
        String[][] puzAndSol = new String[2][81];
        puzAndSol[0] = candidatesIntToString(puzzle);
        puzAndSol[1] = findSolution(puzAndSol[0]);
        return puzAndSol;
    }

    public boolean testSingleSolution(String[] stringBoard) {
        int[] boardArray = boardStringToInt(stringBoard);
        SudokuGraph board = new SudokuGraph(boardArray);
        return SolverAndGenerator.testSingleSolution(board);
    }

    public String[] findCandidates(String[] stringBoard) {
        int[] boardArray = boardStringToInt(stringBoard);
        SudokuGraph board = new SudokuGraph(boardArray);
        return candidatesIntToString(board.findAllCandidates());
    }

    public HintUI hint(String[] candidates, String[] committedNumbers) {
        Vector<Vector<Integer>> intCands = candidatesStringToInt(candidates);
        int[] committedInts = boardStringToInt(committedNumbers);
        Hint hint = HintCreator.createUserHint(intCands, committedInts);
        if (hint == null) {
            return new Hint("No hints available.");
        }
        return hint;
    }

    public HintUI hintAt(int row, int col, String[] candidates, String[] committedNumbers) {
        Vector<Vector<Integer>> intCands = candidatesStringToInt(candidates);
        int[] committedInts = boardStringToInt(committedNumbers);
        Hint hint = HintCreator.createHintAt(row, col, intCands, committedInts);
        if (hint == null) {
            return new Hint("No hints available for that cell.");
        }
        return hint;
    }

    public HintResponse applyHint(HintUI hint, String[] candidates, String[] committedNums) {
        return HintCreator.applyHint((Hint)hint, candidatesStringToInt(candidates), boardStringToInt(committedNums));
    }

    public LessonUI makeLesson(int lessonType) {
        return new Lesson(lessonType);
    }


	/*---------------------------------Integer/String representation converters----------------------------------*/

    private static int[] boardStringToInt(String[] stringBoard) {
        int[] intBoard = new int[81];
        for (int i = 0; i < 81; i++) {
            if (stringBoard[i].equals("")) {
                intBoard[i] = TypeConstants.BLANK;
            }
            else {
                intBoard[i] = Integer.parseInt("" + stringBoard[i].charAt(0)) - 1;
            }
        }
        return intBoard;
    }

    static Vector<Vector<Integer>> candidatesStringToInt(String[] stringCandidates) {
        Vector<Vector<Integer>> intCandidates = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            Vector<Integer> cands = new Vector<Integer>();
            if (!stringCandidates[i].equals("")) {
                for (int j = 0; j < stringCandidates[i].length(); j++) {
                    cands.add(Integer.parseInt("" + stringCandidates[i].charAt(j)) - 1);
                }
            }
            intCandidates.add(cands);
        }
        return intCandidates;
    }

    static String[] boardIntToString(int[] intBoard) {
        String[] stringBoard = new String[81];
        for (int i = 0; i < 81; i++) {
            if (intBoard[i] == TypeConstants.BLANK) {
                stringBoard[i] = "";
            }
            else {
                stringBoard[i] = new Integer(intBoard[i] + 1).toString();
            }
        }
        return stringBoard;
    }

    static String[] candidatesIntToString(Vector<Vector<Integer>> intCandidates) {
        String[] stringCandidates = new String[81];
        for (int i = 0; i < 81; i++) {
            Vector<Integer> cands = intCandidates.elementAt(i);
            stringCandidates[i] = "";
            if (!cands.isEmpty()) {
                for (int j = 0; j < cands.size(); j++) {
                    stringCandidates[i] += new Integer(cands.elementAt(j) + 1).toString();
                }
            }
        }
        return stringCandidates;
    }
}
