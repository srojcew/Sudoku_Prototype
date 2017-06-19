package com.example.user.sudoku.backend.heuristics.lessons;

import java.util.Vector;

import com.example.user.sudoku.backend.LessonResponse;
import com.example.user.sudoku.backend.SudokuGraph;
import com.example.user.sudoku.backend.heuristics.AreaStruct;
import com.example.user.sudoku.backend.heuristics.CandsPattern;

/*
 * Stephen Rojcewicz
 *
 * Used by Lesson to maintain the state of a lesson.
 *
 */

public class LessonState {
    private Vector<Vector<Integer>> candidates;
    private LessonResponse correctResponse;
    private CandsPattern pattern;
    private int stepsCount;
    private boolean checkCands;
    private SudokuGraph puzzle;
    LessonState(SudokuGraph puz, Vector<Vector<Integer>> cands, LessonResponse response, CandsPattern pttn, int stepsC, boolean testC) {
        candidates = cands;
        correctResponse = response;
        pattern = pttn;
        stepsCount = stepsC;
        checkCands = testC;
        puzzle = puz;
    }

    public LessonState(LessonState state) {
        candidates = LessonFunctions.copyCands(state.candidates);
        correctResponse = new LessonResponse(state.correctResponse);
        if (state.getPattern() == null) {
            pattern = null;
        }
        else {
            pattern = new CandsPattern(state.pattern);
        }
        stepsCount = state.stepsCount;
        checkCands = state.checkCands;
        puzzle = new SudokuGraph(state.puzzle.valuesArray());
    }

    public Vector<Vector<Integer>> getCandidates() {
        return candidates;
    }
    public LessonResponse getCorrectResponse() {
        return correctResponse;
    }
    public CandsPattern getPattern() {
        return pattern;
    }
    public int getStepsCount() {
        return stepsCount;
    }
    public boolean checkCands() {
        return checkCands;
    }
    public SudokuGraph getPuzzle() {
        return puzzle;
    }

    public void update(SudokuGraph puz, Vector<Vector<Integer>> cands, LessonResponse response, CandsPattern pttn, int stepsC, boolean testC) {
        candidates = cands;
        correctResponse = response;
        pattern = pttn;
        stepsCount = stepsC;
        checkCands = testC;
        puzzle = puz;
    }
}
