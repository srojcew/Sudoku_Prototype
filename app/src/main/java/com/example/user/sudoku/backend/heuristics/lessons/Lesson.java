package com.example.user.sudoku.backend.heuristics.lessons;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import com.example.user.sudoku.backend.LessonFormatting;
import com.example.user.sudoku.backend.LessonResponse;
import com.example.user.sudoku.backend.LessonStep;
import com.example.user.sudoku.backend.LessonUI;
import com.example.user.sudoku.backend.SolverAndGenerator;
import com.example.user.sudoku.backend.SudokuGraph;
import com.example.user.sudoku.backend.TypeConstants;
import com.example.user.sudoku.backend.heuristics.*;
import com.example.user.sudoku.backend.heuristics.AreaStruct;

/*
 * Stephen Rojcewicz
 *
 * This is the backend class for a lesson. Represents a lesson as a finite state machine.
 *
 */
public class Lesson implements LessonUI {

    private Vector<Vector<Integer>> emptyGrid;
    int type;
    private String introMessage;
    private LessonState currentState;
    private Random rand;

    public Lesson(int tp) {
        rand = new Random(new Date().getTime());
        emptyGrid = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            emptyGrid.add(new Vector<Integer>());
        }
        type = tp;
        if (type == TypeConstants.H_SINGLE) {
            initAsHSingle();
        }
        else if (type == TypeConstants.N_SINGLE) {
            initAsNSingle();
        }
    }
    /**
     * Returns the next lesson step according to the given LessonResponse
     */
    public LessonStep getNextStep(LessonResponse response, LessonStep step) {
        if (type == TypeConstants.H_SINGLE) {
            if (step != null) {
                currentState = step.getState();
            }
            return nextHiddenSingleStep(response);
        }
        else if (type == TypeConstants.N_SINGLE) {
            if (step != null) {
                currentState = step.getState();
            }
            return nextNSingleStep(response);
        }
        return null;
    }
    public boolean hasNextStep(LessonStep step) {
        if (type == TypeConstants.H_SINGLE) {
            if (step == null) {
                return (currentState.getStepsCount() < 20);
            }
            return (step.getState().getStepsCount() < 20);
        }
        else if (type == TypeConstants.N_SINGLE) {
            if (step == null) {
                return (currentState.getStepsCount() < 18);
            }
            return (step.getState().getStepsCount() < 18);
        }
        return false;
    }

    private void initAsHSingle() {
        introMessage = createHSingleIntro();
        SudokuGraph puzzle = SolverAndGenerator.makePuzzleGraph(55);
        while (!HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates())) {
            puzzle = SolverAndGenerator.makePuzzleGraph(55);
        }
        currentState = new LessonState(puzzle, emptyGrid, new LessonResponse(emptyGrid, null), null, 0, false);
    }

    private void initAsNSingle() {
        introMessage = createNSIntro();
        SudokuGraph puzzle = SolverAndGenerator.makePuzzleGraph(50);
        while (!HeuristicsSolvers.solveWithNakedSingles(puzzle.findAllCandidates(), 0)) {
            puzzle = SolverAndGenerator.makePuzzleGraph(50);
        }
        currentState = new LessonState(puzzle, emptyGrid, new LessonResponse(emptyGrid, null), null, 0, false);
    }


	/*
	 * ------------------------------------------------------------------naked singles---------------------------------------------------
	 */

    private LessonStep nextNSingleStep(LessonResponse response) {
        if (response.hasCellClicked() && !currentState.getCorrectResponse().hasCellClicked()) {
            return null;
        }
        if (currentState.getStepsCount() == 0) {
            return nSingleIntro();
        }
        if (currentState.getStepsCount() == 1) {
            return nSingleBegin();
        }
        if (currentState.getStepsCount() < 12) {
            return nSingleCandElim();
        }
        if (currentState.getStepsCount() == 12 || currentState.getStepsCount() == 14) {
            return nSingleNumPrompt();
        }
        if (currentState.getStepsCount() == 13 || currentState.getStepsCount() == 15) {
            return nSingleNumResponse(response);
        }
        if (currentState.getStepsCount() == 16) {
            return nSingleUnknownPrompt();
        }
        if (currentState.getStepsCount() == 17) {
            return nSingleUnknownResponse(response);
        }
        return null;
    }

    private LessonStep nSingleIntro() {
        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), currentState.getCorrectResponse(), currentState.getPattern(), currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(introMessage, emptyGrid, null, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep nSingleBegin() {
        SudokuGraph puzzle = currentState.getPuzzle();
        Vector<Vector<Integer>> committedNums = puzzle.committedNumberes();
        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(puzzle.findAllCandidates(), puzzle.valuesArray());
        CandsPattern nakedSingle = nakedSingles.elementAt(rand.nextInt(nakedSingles.size()));
        String message = "The naked single technique is the only strategy you need to solve this puzzle. The highlighted cell is an example of a naked single " +
                " because there is only one number that can be placed in this cell without immediately violationg the rules of Sudoku." +
                " This number is " + (nakedSingle.getNums()[0] + 1) + ". Click Next to see why this cell's number must be " + (nakedSingle.getNums()[0] + 1) + ".";
        currentState.update(puzzle, committedNums, new LessonResponse(committedNums, null), nakedSingle, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, committedNums, null, nakedSingle.getCells(), null, null, null);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }
    private LessonStep nSingleCandElim() {
        SudokuGraph puzzle = currentState.getPuzzle();
        CandsPattern nakedSingle = currentState.getPattern();
        int correctNum = nakedSingle.getNums()[0];
        int wrongNum;
        String message = "";
        LessonFormatting beforeFormat;
        if (correctNum == 8 && currentState.getStepsCount() - 2 == correctNum) {
            message = "Thus the number in this cell must be " + (correctNum + 1) + ". Click next to continue.";
            puzzle.putValueAt(nakedSingle.getCells().firstElement(), correctNum);
            currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(puzzle.committedNumberes(), null), nakedSingle, currentState.getStepsCount() + 2, false);
            beforeFormat = new LessonFormatting(message, puzzle.committedNumberes(), null, nakedSingle.getCells(), null, null, null);
        }
        else if (currentState.getStepsCount() < 11) {
            if (currentState.getStepsCount() == 2) {
                message += "It is not ";
            }
            else if (currentState.getStepsCount() == 3 || currentState.getStepsCount() == 4) {
                message += "Nor is it ";

            }
            else {
                message += "And it is not ";
            }
            if (currentState.getStepsCount() - 2 == correctNum) {
                wrongNum = currentState.getStepsCount() - 1;
                currentState.update(puzzle, currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), null), nakedSingle, currentState.getStepsCount() + 2, false);
            }
            else {
                wrongNum = currentState.getStepsCount() - 2;
                currentState.update(puzzle, currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), null), nakedSingle, currentState.getStepsCount() + 1, false);
            }
            ConflictStruct conflict = LessonFunctions.findConflictingCell(nakedSingle.getCells().firstElement(), wrongNum, puzzle.committedNumberes());
            message += (wrongNum + 1) + " because that number occurs in this cell's " + conflict.getArea().getName() + ". Click next.";
            Vector<Integer> conflictCell = new Vector<Integer>();
            conflictCell.add(conflict.getCell());
            beforeFormat = new LessonFormatting(message, currentState.getCandidates(), null, nakedSingle.getCells(), conflictCell, null, null);
        }
        else {
            message = "Thus the number in this cell must be " + (correctNum + 1) + ". Click next to continue";
            puzzle.putValueAt(nakedSingle.getCells().firstElement(), correctNum);
            currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(puzzle.committedNumberes(), null), nakedSingle, currentState.getStepsCount() + 1, false);
            beforeFormat = new LessonFormatting(message, puzzle.committedNumberes(), null, nakedSingle.getCells(), null, null, null);
        }
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep nSingleNumPrompt() {
        SudokuGraph puzzle = currentState.getPuzzle();
        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(puzzle.findAllCandidates(), puzzle.valuesArray());
        CandsPattern nakedSingle = nakedSingles.elementAt(rand.nextInt(nakedSingles.size()));
        String message = "This cell has a naked single. What is it? Type the number in the cell. Then click Next.";
        Vector<Vector<Integer>> correctGrid = puzzle.committedNumberes();
        correctGrid.elementAt(nakedSingle.getCells().firstElement()).add(nakedSingle.getNums()[0]);
        currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(correctGrid, null), nakedSingle, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, puzzle.committedNumberes(), null, nakedSingle.getCells(), null, null, nakedSingle.getCells());
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep nSingleNumResponse(LessonResponse response) {
        CandsPattern nakedSingle = currentState.getPattern();
        String message;
        Vector<Vector<Integer>> responseGrid = response.getIntegerGrid();
        String promptMessage = "This cell has a naked single. What is it? Type the number in the cell. Then click Next.";
        if (responseGrid.elementAt(nakedSingle.getCells().firstElement()).isEmpty()) {
            message = "No. You still need to enter a number in the highlighted cell. " + promptMessage;
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), null, nakedSingle.getCells(), null, null, nakedSingle.getCells());
            return new LessonStep(beforeFormat, null, null, false, currentState);
        }
        else if (responseGrid.elementAt(nakedSingle.getCells().firstElement()).size() == 1) {
            if (responseGrid.elementAt(nakedSingle.getCells().firstElement()).firstElement().equals(nakedSingle.getNums()[0])) {
                SudokuGraph puzzle = currentState.getPuzzle();
                puzzle.putValueAt(nakedSingle.getCells().firstElement(), nakedSingle.getNums()[0]);
                message = "Correct. Click next to continue.";
                currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(currentState.getCandidates(), null), nakedSingle, currentState.getStepsCount() + 1, false);
                LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), null, nakedSingle.getCells(), null, null, null);
                return new LessonStep(beforeFormat, null, null, false, currentState);
            }
            int wrongNum = responseGrid.elementAt(nakedSingle.getCells().firstElement()).firstElement();
            ConflictStruct conflict = LessonFunctions.findConflictingCell(nakedSingle.getCells().firstElement(), wrongNum, currentState.getCandidates());
            message = "No. " + (wrongNum + 1) + " already occurs in this cell's " + conflict.getArea().getName() + "." +
                    " Click Undo.";
            Vector<Integer> conflictCells = new Vector<Integer>();
            conflictCells.add(conflict.getCell());
            LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, nakedSingle.getCells(), conflictCells, null, null);
            LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, currentState.getCandidates(), null, nakedSingle.getCells(), null, null, nakedSingle.getCells());
            return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
        }
        else {
            message = "No. You should enter only one number. You do not need to enter multiple candidates. Click Undo.";
            LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, nakedSingle.getCells(), null, null, null);
            LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, currentState.getCandidates(), null, nakedSingle.getCells(), null, null, nakedSingle.getCells());
            return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
        }
    }

    private LessonStep nSingleUnknownPrompt() {
        SudokuGraph puzzle = SolverAndGenerator.makePuzzleGraph(45);
        Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(puzzle.findAllCandidates(), puzzle.valuesArray());
        while (nakedSingles.size() < 7) {
            puzzle = SolverAndGenerator.makePuzzleGraph(45);
            nakedSingles = HeuristicsAnalyzer.findNakedSingles(puzzle.findAllCandidates(), puzzle.valuesArray());
        }
        String message = "Use the keyboard to fill in any naked single in this puzzle. Enter only one naked single. " + "Click Next when finished.";
        currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(puzzle.committedNumberes(), null), null, currentState.getStepsCount() + 1, false);
        Vector<Integer> editableCells = new Vector<Integer>();
        // make all blank cells editable
        for (int i = 0; i < 81; i++) {
            if (puzzle.valuesArray()[i] == TypeConstants.BLANK) {
                editableCells.add(i);
            }
        }
        LessonFormatting beforeFormat = new LessonFormatting(message, puzzle.committedNumberes(), null, null, null, null, editableCells);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep nSingleUnknownResponse(LessonResponse response) {
        String message;
        String promptMessage = "Use the keyboard to fill in any naked single in this puzzle. Enter only one naked single. " + "Click Next when finished.";
        Vector<Integer> editableCells = new Vector<Integer>();
        // make all blank cells editable
        for (int i = 0; i < 81; i++) {
            if (currentState.getCandidates().elementAt(i).isEmpty()) {
                editableCells.add(i);
            }
        }

        Vector<Vector<Integer>> responseGrid = response.getIntegerGrid();
        Vector<Vector<Integer>> oldGrid = currentState.getCandidates();
        int modificationsCount = 0;
        int modifiedCell = -1;
        // check that only one number has been entered by the user
        for (int i = 0; i < 81; i++) {
            if (oldGrid.elementAt(i).isEmpty()) {
                if (responseGrid.elementAt(i).size() > 1) {
                    message = "No. You should enter only one number. You do not need to enter multiple candidates. Click undo.";
                    Vector<Integer> errorCells = new Vector<Integer>();
                    errorCells.add(i);
                    LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, null, errorCells, null, null);
                    LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, oldGrid, null, null, null, null, editableCells);
                    return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
                }
                else if (responseGrid.elementAt(i).size() == 1) {
                    modificationsCount++;
                    modifiedCell = i;
                }
            }
        }

        if (modificationsCount == 0) {
            message = "No. You still need to enter a number. " + promptMessage;
            LessonFormatting beforeFormat = new LessonFormatting(message, oldGrid, null, null, null, null, editableCells);
            return new LessonStep(beforeFormat, null, null, false, currentState);
        }
        else if (modificationsCount == 1) {
            // check if the user has entered a valid naked single
            Vector<Vector<Integer>> candidates = currentState.getPuzzle().findAllCandidates();
            Vector<Integer> nSingleCell = new Vector<Integer>();
            nSingleCell.add(modifiedCell);
            if (candidates.elementAt(modifiedCell).size() == 1) {
                if (responseGrid.elementAt(modifiedCell).firstElement().equals(candidates.elementAt(modifiedCell).firstElement())) {
                    message = "Correct. The only candidate for that cell is " + (responseGrid.elementAt(modifiedCell).firstElement() + 1) + ". You have completed the naked singles lesson.";
                    currentState.update(currentState.getPuzzle(), responseGrid, new LessonResponse(responseGrid, null), null, currentState.getStepsCount() + 1, false);
                    LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, nSingleCell, null, null, null);
                    return new LessonStep(beforeFormat, null, null, false, currentState);
                }
                else {
                    message = "There is a naked single in that cell, but it is not " + (responseGrid.elementAt(modifiedCell).firstElement() + 1) + ". Click Undo.";
                    LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, null, nSingleCell, null, null);
                    LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, oldGrid, null, null, null, null, editableCells);
                    return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
                }
            }
            else {
                message = "No. Since that cell has more than one candidate, it does not have a naked single. Click Undo.";
                LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, null, nSingleCell, null, null);
                LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, oldGrid, null, null, null, null, editableCells);
                return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
            }
        }
        else { // more than one cell was modified
            message = "No. You should enter a number in only one cell. Click Undo.";
            LessonFormatting beforeFormat = new LessonFormatting(message, responseGrid, null, null, null, null, null);
            LessonFormatting afterActionFormat = new LessonFormatting(promptMessage, oldGrid, null, null, null, null, editableCells);
            return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
        }
    }


	/*
	 * ------------------------------------------------------------------hidden singles--------------------------------------------------
	 */

    private LessonStep nextHiddenSingleStep(LessonResponse response) {
        if (response.hasCellClicked() && !currentState.getCorrectResponse().hasCellClicked()) {
            return null;
        }

        if (currentState.checkCands()) {
            return checkCandidates();
        }

        if (currentState.getStepsCount() == 0) {
            return hSingleIntro();
        }
        if (currentState.getStepsCount() == 1) {
            return hSingleBegin();
        }
        if (currentState.getStepsCount() == 2) {
            return hSingleExample();
        }
        if (currentState.getStepsCount() == 3) {
            return knownHSinglePrompt();
        }
        if (currentState.getStepsCount() == 4) {
            return knownHSingleResponse(response);
        }
        if (currentState.getStepsCount() == 5) {
            return unknownHSinglePrompt();
        }
        if (currentState.getStepsCount() == 6) {
            return unknownHSingleResponse(response);
        }
        if (currentState.getStepsCount() == 7 || currentState.getStepsCount() == 9) {
            return userDoHSinglePrompt();
        }
        if (currentState.getStepsCount() == 8 || currentState.getStepsCount() == 10) {
            return userDoHSingleResponse(response);
        }
        if (currentState.getStepsCount() == 11) {
            return hSingleBeginNoCands();
        }
        if (currentState.getStepsCount() == 12 || currentState.getStepsCount() == 14 || currentState.getStepsCount() == 16) {
            return hSingleKnownNoCPrompt();
        }
        if (currentState.getStepsCount() == 13 || currentState.getStepsCount() == 15 || currentState.getStepsCount() == 17) {
            return hSingleKnownNoCResponse(response);
        }
        if (currentState.getStepsCount() == 18) {
            return hSingleUnknownNoCPrompt();
        }
        if (currentState.getStepsCount() == 19) {
            return hSingleUnknownNoCResponse(response);
        }
        return null;
    }

    private LessonStep checkCandidates() {
        if (currentState.getPattern() == null) {
            Vector<Vector<Integer>> oldCands = currentState.getCandidates();
            Vector<Vector<Integer>> cands = new SudokuGraph(oldCands).findAllCandidates();

            // check for single candidates
            Vector<Integer> singleCandCells = new Vector<Integer>();
            for (int i = 0; i < 81; i++) {
                if(oldCands.elementAt(i).size() != 1 && cands.elementAt(i).size() == 1) {
                    singleCandCells.add(i);
                    oldCands.set(i, cands.elementAt(i));
                }
            }
            String message;
            currentState.update(currentState.getPuzzle(), cands, new LessonResponse(cands, null), currentState.getPattern(), currentState.getStepsCount(), false);
            if (!singleCandCells.isEmpty()) {
                message = "Often, a puzzle has some blank cells that have only one candidate. These cells are highlighted in blue. These candidates are called" +
                        " naked singles. We could commit them right now, but we'll treat them as candidates in this lesson. If you are not familiar with naked singles, " +
                        " review the naked singles lesson. Click \"Show Other Candidates\" to see the rest of the candidates.";
                LessonFormatting beforeFormat = new LessonFormatting(message, oldCands, singleCandCells, null, null, singleCandCells, null);
                LessonFormatting afterActionFormat = new LessonFormatting("Here are all of the candidates. Click Next to continue.", cands, singleCandCells, null, null, null, null);
                //checkCands = true;
                return new LessonStep(beforeFormat, afterActionFormat, "Show Other Candidates", false, currentState);
            }
            else {
                message = "Here are the candidates. Click next to continue";
                LessonFormatting beforeFormat = new LessonFormatting(message, cands, null, null, null, null, null);
                return new LessonStep(beforeFormat, null, null, false, currentState);
            }
        }
        else {
            Vector<Vector<Integer>> currentCands = currentState.getCandidates();
            Vector<Vector<Integer>> oldCands = LessonFunctions.copyCands(currentCands);
            int committed = currentState.getPattern().getNums()[0];
            Vector<Integer> cell = currentState.getPattern().getCells();
            Vector<Integer> modifiedCandCells = LessonFunctions.updateCandidates(currentCands, committed, cell.firstElement());

            // create a list of cells that have single candidates so that these cells can be formatted correctly
            Vector<Integer> singleCandCells = new Vector<Integer>();
            for (int i = 0; i < modifiedCandCells.size(); i++) {
                int cellNum = modifiedCandCells.elementAt(i);
                if (currentCands.elementAt(cellNum).size() == 1) {
                    singleCandCells.add(cellNum);
                }
            }

            String message;
            String messageEnd;
            switch(currentState.getPattern().getArea().getType()) {
                case AreaStruct.ROW:
                    messageEnd = "column and region. There is no need to check the row ";
                    break;
                case AreaStruct.COL:
                    messageEnd = "row and region. There is no need to check the column ";
                    break;
                default:
                    messageEnd = "row and column. There is no need to check the region ";
                    break;
            }

            messageEnd += "because that is where the hidden single was.";

            currentState.update(currentState.getPuzzle(), currentCands, new LessonResponse(currentCands, null), currentState.getPattern(), currentState.getStepsCount(), false);
            if (modifiedCandCells.isEmpty()) {
                message = "Now that this cell's number is known, the candidate " + (committed + 1) + " can be removed from the other cells in this cell's " + messageEnd +
                        " In this case, there are no candidates to remove.";
                LessonFormatting beforeFormat = new LessonFormatting(message, currentCands, null, cell, null, null, null);
                return new LessonStep(beforeFormat, null, null, false, currentState);
            }
            else {
                message = "Now that we know this cell has the number " + (committed + 1) + ", the candidate " + (committed + 1) + " can be removed from the other cells in this cell's " + messageEnd + " Click \"Update Candidates\" to see the changes.";
                LessonFormatting beforeFormat = new LessonFormatting(message, oldCands, modifiedCandCells, cell, null, null, null);
                LessonFormatting afterActionFormat = new LessonFormatting("Click next to continue.", currentCands, modifiedCandCells, cell, null, singleCandCells, null);
                return new LessonStep(beforeFormat, afterActionFormat, "Update Candidates", false, currentState);
            }
        }
    }

    private LessonStep hSingleIntro() {
        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), currentState.getCorrectResponse(), currentState.getPattern(), currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(introMessage, emptyGrid, null, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep hSingleBegin() {
        Vector<Vector<Integer>> committedNums = currentState.getPuzzle().committedNumberes();
        String message = "The hidden single technique is the only strategy you need to solve this puzzle. It is easier to spot hidden singles" +
                " when the candidates for each blank cell are written down. In game mode, you can reveal the" +
                " candidates with the show/hide candidates button. Of course, you can also type them in yourself. Click Next to reveal the candidates.";
        currentState.update(currentState.getPuzzle(), committedNums, new LessonResponse(committedNums, null), null, currentState.getStepsCount() + 1, true);
        LessonFormatting beforeFormat = new LessonFormatting(message, committedNums, null, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep hSingleExample() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(currentState.getPuzzle().findAllCandidates());
        CandsPattern hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
        AreaStruct selectedArea = hiddenSingle.getArea();
        Vector<Vector<Integer>> candidates = currentState.getPuzzle().findAllCandidates();
        Vector<Integer> cellsToHighlightDark = hiddenSingle.getCells();

        Vector<Vector<Integer>> afterActionCands = LessonFunctions.applyHiddenSingle(candidates, hiddenSingle);

        String message = "Here is a hidden single. Do you see that the only place" +
                " we could put a " + (hiddenSingle.getNums()[0] + 1) + " in this " + selectedArea.getName() + " is the darker cell?";

        currentState.update(currentState.getPuzzle(), afterActionCands, new LessonResponse(currentState.getCandidates(), null), hiddenSingle, currentState.getStepsCount() + 1, true);
        Vector<Integer> cellsInArea = selectedArea.getCells();
        LessonFormatting beforeFormat = new LessonFormatting(message, candidates, cellsInArea, cellsToHighlightDark, null, null, null);
        LessonFormatting afterActionFormat = new LessonFormatting(message, afterActionCands, cellsInArea, cellsToHighlightDark, null, null, null);
        String actionMessage = "Make this cell a " + (hiddenSingle.getNums()[0] + 1);
        return new LessonStep(beforeFormat, afterActionFormat, actionMessage, false, currentState);
    }

    private LessonStep knownHSinglePrompt() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(currentState.getCandidates());
        CandsPattern hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
        AreaStruct selectedArea = hiddenSingle.getArea();
        Vector<Integer> cells = hiddenSingle.getCells();

        String message = "There is only one place to put " + (hiddenSingle.getNums()[0] + 1) + " in this " + selectedArea.getName() + ". Click on it.";

        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), cells.elementAt(0)), hiddenSingle, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), selectedArea.getCells(), null, null, null, null);
        return new LessonStep(beforeFormat, null, null, true, currentState);
    }

    private LessonStep knownHSingleResponse(LessonResponse response) {
        Vector<Integer> cellsToHighlightLight = currentState.getPattern().getArea().getCells();
        String message;
        if (currentState.getCorrectResponse().getCellClicked().equals(response.getCellClicked())) {
            Vector<Vector<Integer>> newCandidates = LessonFunctions.applyHiddenSingle(currentState.getCandidates(), currentState.getPattern());

            Vector<Integer> cellsToHighlightDark = currentState.getPattern().getCells();

            message = "Correct. That is the only cell in this " + currentState.getPattern().getArea().getName() + " that can contain " + (currentState.getPattern().getNums()[0] + 1) + ".";

            currentState.update(currentState.getPuzzle(), newCandidates, new LessonResponse(newCandidates, null), currentState.getPattern(), currentState.getStepsCount() + 1, true);
            LessonFormatting beforeFormat = new LessonFormatting(message, newCandidates, cellsToHighlightLight, cellsToHighlightDark, null, null, null);
            return new LessonStep(beforeFormat, null, null, false, currentState);
        }
        else {
            AreaStruct area = currentState.getPattern().getArea();
            if (area.getCells().contains(response.getCellClicked())) {
                ConflictStruct conflict = LessonFunctions.findConflictingCell(response.getCellClicked(), currentState.getPattern().getNums()[0], currentState.getCandidates());
                message = "No. The " + (currentState.getPattern().getNums()[0] + 1) + " cannot go there because there is already " + (currentState.getPattern().getNums()[0] + 1) + " in that " + conflict.getArea().getName();
                Vector<Integer> cellsToHighlightRed = new Vector<Integer>();
                cellsToHighlightRed.add(conflict.getCell());
                LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsToHighlightLight, null, cellsToHighlightRed, null, null);
                return new LessonStep(beforeFormat, null, null, true, currentState);
            }
            else {
                message = "No. We are looking for a hidden single in the highlighted " + currentState.getPattern().getArea().getName();
                LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsToHighlightLight, null, null, null, null);
                return new LessonStep(beforeFormat, null, null, true, currentState);
            }
        }
    }

    private LessonStep unknownHSinglePrompt() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(currentState.getCandidates());

        // select a pattern in an area that does not have any other patterns in it
        CandsPattern hiddenSingle = LessonFunctions.choosePatternUniqueInArea(hiddenSingles, rand);
        String message;

        if (hiddenSingle == null) { // the rare case in which no area contains only one pattern
            hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
            message = "Click on a cell with a hidden single in this " + hiddenSingle.getArea().getName() + ".";
        }
        else {
            message = "Click on the cell with the hidden single in this " + hiddenSingle.getArea().getName() + ".";
        }
        Vector<Integer> cells = hiddenSingle.getCells();

        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), cells.elementAt(0)), hiddenSingle, currentState.getStepsCount() + 1, false);
        Vector<Integer> cellsInArea = hiddenSingle.getArea().getCells();
        LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsInArea, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, true, currentState);
    }

    private LessonStep unknownHSingleResponse(LessonResponse response) {
        Vector<Integer> cellsToHighlightLight = currentState.getPattern().getArea().getCells();
        String message;
        if (currentState.getCorrectResponse().getCellClicked().equals(response.getCellClicked())) {
            Vector<Vector<Integer>> newCandidates = LessonFunctions.applyHiddenSingle(currentState.getCandidates(), currentState.getPattern());
            Vector<Integer> cellsToHighlightDark = currentState.getPattern().getCells();

            message = "Correct. That is the only cell in this " + currentState.getPattern().getArea().getName() + " that can contain " + (currentState.getPattern().getNums()[0] + 1) + ". Click Next to continue.";

            currentState.update(currentState.getPuzzle(), newCandidates, new LessonResponse(newCandidates, null), currentState.getPattern(), currentState.getStepsCount() + 1, true);
            LessonFormatting beforeFormat = new LessonFormatting(message, newCandidates, cellsToHighlightLight, cellsToHighlightDark, null, null, null);
            return new LessonStep(beforeFormat, null, null, false, currentState);
        }
        else {
            AreaStruct area = currentState.getPattern().getArea();
            if (response.getIntegerGrid().elementAt(response.getCellClicked()).size() == 1) {
                if (currentState.getCandidates().elementAt(response.getCellClicked()).size() == 1) {
                    if (currentState.getCandidates().elementAt(response.getCellClicked()).firstElement().equals(response.getIntegerGrid().elementAt(response.getCellClicked()).firstElement())) {
                        message = "That is a single candidate, but it is a naked single, not a hidden single. Click on the cell with the hidden single.";
                    }
                    else {
                        message = "That is a single candidate, but it is a naked single, not a hidden single. Also, you changed the number in that cell. It has been corrected.";
                    }
                }
                else {
                    message = "No. you do not need to edit the numbers in the cell. Click on the cell with the hidden single.";
                }
            }
            else {
                if(area.getCells().contains(response.getCellClicked())) {
                    message = "No. All of the candidates in that cell occur somewhere else in this " + currentState.getPattern().getArea().getName() + ".";
                }
                else{
                    message = "No. We are looking for a hidden single in the highlighted " + currentState.getPattern().getArea().getName();
                }
            }
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsToHighlightLight, null, null, null, null);
            return new LessonStep(beforeFormat, null, null, true, currentState);
        }
    }

    private LessonStep userDoHSinglePrompt() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(currentState.getCandidates());
        // select a pattern in an area that does not have any other patterns in it
        CandsPattern hiddenSingle = LessonFunctions.choosePatternUniqueInArea(hiddenSingles, rand);

        String message;

        if (hiddenSingle == null) { // the rare case in which no area contains only one pattern
            hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
            message = "The number " + hiddenSingle.getNums()[0] + " is a hidden single in this " + hiddenSingle.getArea().getName() + ". Use the keyboard to enter the correct number. When finished, click Next.";
        }
        else {
            message = "There is a hidden single in this " + hiddenSingle.getArea().getName() + ". Use the keyboard to modify the candidates so that the hidden single is the only number in its cell. When finished, click Next.";
        }
        Vector<Vector<Integer>> newCands = LessonFunctions.applyHiddenSingle(currentState.getCandidates(), hiddenSingle);
        Vector<Integer> cellsInArea = hiddenSingle.getArea().getCells();

        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(newCands, null), hiddenSingle, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsInArea, null, null, null, cellsInArea);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep userDoHSingleResponse(LessonResponse response) {
        int correctCell = currentState.getPattern().getCells().firstElement();
        Vector<Integer> cellsInArea = currentState.getPattern().getArea().getCells();
        Vector<Vector<Integer>> expectedCands = currentState.getCorrectResponse().getIntegerGrid();
        Vector<Vector<Integer>> responseCands = response.getIntegerGrid();
        String message;
        String afterActionMessage = "You still need to remove the incorrect candidates from the hidden single's cell. Use the keyboard to remove the incorrect candidates, and then click Next.";
        // TODO: check that enterd single is the solution number
        for (int i = 0; i < cellsInArea.size(); i++) {
            int cell = cellsInArea.elementAt(i);
            for (int j = 0; j < expectedCands.size(); j++) {
                if (responseCands.elementAt(cell).size() == 1) {
                    if (!(expectedCands.elementAt(cell).size() == 1 && expectedCands.elementAt(cell).firstElement().equals(responseCands.elementAt(cell).firstElement()))) {
                        int wrongNum = responseCands.elementAt(cell).firstElement();
                        Vector<Integer> conflictingCells = LessonFunctions.findCellsThatContain(responseCands, wrongNum, currentState.getPattern().getArea());
                        message = "No, " + (wrongNum + 1) + " is not a hidden single because it is a candidate in ";
                        message += (conflictingCells.size() > 2)? "these other cells." : "this other cell.";
                        message += " Click \"Replace Candidates\"";
                        LessonFormatting beforeFormat = new LessonFormatting(message, response.getIntegerGrid(), cellsInArea, null, conflictingCells, null, null);
                        LessonFormatting afterActionFormat = new LessonFormatting(afterActionMessage, currentState.getCandidates(), cellsInArea, null, null, null, cellsInArea);
                        return new LessonStep(beforeFormat, afterActionFormat, "Replace Candidates", false, currentState);
                    }
                    else {
                        if (cell == correctCell) {
                            message = "Correct. Click Next to continue.";
                            currentState.update(currentState.getPuzzle(), expectedCands, new LessonResponse(expectedCands, null), currentState.getPattern(), currentState.getStepsCount() + 1, true);
                            LessonFormatting beforeFormat = new LessonFormatting(message, expectedCands, cellsInArea, null, null, null, null);
                            return new LessonStep(beforeFormat, null, null, false, currentState);
                        }
                    }
                }
            }
        }
        message = "No. You still need to remove the incorrect candidates from the hidden single's cell. Click Undo.";
        LessonFormatting beforeFormat = new LessonFormatting(message, response.getIntegerGrid(), cellsInArea, null, null, null, null);
        LessonFormatting afterActionFormat = new LessonFormatting(afterActionMessage, currentState.getCandidates(), cellsInArea, null, null, null, cellsInArea);
        return new LessonStep(beforeFormat, afterActionFormat, "Undo", false, currentState);
    }

    private LessonStep hSingleBeginNoCands() {
        SudokuGraph puzzle = SolverAndGenerator.makePuzzleGraph(55);
        while (!HeuristicsSolvers.solveWithHiddenSingles(puzzle.findAllCandidates())) {
            puzzle = SolverAndGenerator.makePuzzleGraph(55);
        }
        String message = "You do not need to write down the candidates to use the hidden single technique. Here is a new puzzle. This time, we will identify hidden singles without " +
                "the aid of candidates. Click Next to continue.";
        currentState.update(puzzle, puzzle.committedNumberes(), new LessonResponse(puzzle.committedNumberes(), null), null, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, puzzle.committedNumberes(), null, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, false, currentState);
    }

    private LessonStep hSingleKnownNoCPrompt() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(new SudokuGraph(currentState.getCandidates()).findAllCandidates());
        CandsPattern hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
        AreaStruct area = hiddenSingle.getArea();
        int single = hiddenSingle.getNums()[0];
        int correctCell = hiddenSingle.getCells().firstElement();
        Vector<Integer> cells = area.getCells();
        String message = "Where must the " + (single + 1) + " be in this " + area.getName() + "? Click on the cell.";
        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), correctCell), hiddenSingle, currentState.getStepsCount() + 1, false);
        LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cells, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, true, currentState);
    }

    private LessonStep hSingleKnownNoCResponse(LessonResponse response) {
        String promptMessage = "Where must the " + (currentState.getPattern().getNums()[0] + 1) + " be in this " + currentState.getPattern().getArea().getName() + "? Click on the cell.";
        if (response.getCellClicked().equals(currentState.getCorrectResponse().getCellClicked())) {
            String message = "Correct. Click next to continue.";
            // apply the hidden single rule
            currentState.getCandidates().elementAt(response.getCellClicked()).add(currentState.getPattern().getNums()[0]);
            currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), null), currentState.getPattern(), currentState.getStepsCount() + 1, false);
            Vector<Integer> cell = new Vector<Integer>();
            cell.add(response.getCellClicked());
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), null, cell, null, null, null);
            return new LessonStep(beforeFormat, null, null, false, currentState);

        }
        else if (!currentState.getPattern().getArea().getCells().contains(response.getCellClicked())) {
            String message = "No. Choose a cell in the highlighted area. " + promptMessage;
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), currentState.getPattern().getArea().getCells(), null, null, null, null);
            return new LessonStep(beforeFormat, null, null, true, currentState);
        }
        else {
            int num = currentState.getPattern().getNums()[0];
            ConflictStruct conflict = LessonFunctions.findConflictingCell(response.getCellClicked(), num, currentState.getCandidates());
            Vector<Integer> conflictCell = new Vector<Integer>();
            conflictCell.add(conflict.getCell());
            Vector<Integer> areaCells = currentState.getPattern().getArea().getCells();
            String message = "No. The " + (num + 1) + " cannot go there because of the " + (num + 1) + " which is already in that " + conflict.getArea().getName() + "." + promptMessage;
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), areaCells, null, conflictCell, null, null);
            return new LessonStep(beforeFormat, null, null, true, currentState);
        }
    }

    private LessonStep hSingleUnknownNoCPrompt() {
        Vector<CandsPattern> hiddenSingles = HeuristicsAnalyzer.findHiddenSingles(new SudokuGraph(currentState.getCandidates()).findAllCandidates());
        // select a pattern in an area that does not have any other patterns in it
        CandsPattern hiddenSingle = LessonFunctions.choosePatternUniqueInArea(hiddenSingles, rand);
        String message;

        if (hiddenSingle == null) { // the rare case in which no area contains only one pattern
            hiddenSingle = hiddenSingles.elementAt(rand.nextInt(hiddenSingles.size()));
            message = "Click on a cell with a hidden single in this " + hiddenSingle.getArea().getName() + ".";
        }
        else {
            message = "Click on the cell with the hidden single in this " + hiddenSingle.getArea().getName() + ".";
        }

        Vector<Integer> cell = hiddenSingle.getCells();
        currentState.update(currentState.getPuzzle(), currentState.getCandidates(), new LessonResponse(currentState.getCandidates(), cell.firstElement()), hiddenSingle, currentState.getStepsCount() + 1, false);
        Vector<Integer> cellsInArea = hiddenSingle.getArea().getCells();
        LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsInArea, null, null, null, null);
        return new LessonStep(beforeFormat, null, null, true, currentState);
    }

    private LessonStep hSingleUnknownNoCResponse(LessonResponse response) {
        int correctCell = currentState.getCorrectResponse().getCellClicked();
        Vector<Integer> cellsInArea = currentState.getPattern().getArea().getCells();
        String message;
        if (response.getCellClicked().equals(correctCell)) {
            Vector<Vector<Integer>> updatedGrid = currentState.getCandidates();
            updatedGrid.elementAt(correctCell).clear();
            updatedGrid.elementAt(correctCell).add(currentState.getPattern().getNums()[0]);

            Vector<Integer> cell = new Vector<Integer>();
            cell.add(correctCell);

            message = "Correct. That is the only cell in this " + currentState.getPattern().getArea().getName() + " that can be " + (currentState.getPattern().getNums()[0] + 1) + "." +
                    " You have completed the hidden singles lesson.";

            currentState.update(currentState.getPuzzle(), updatedGrid, new LessonResponse(updatedGrid, null), currentState.getPattern(), currentState.getStepsCount() + 1, false);
            LessonFormatting beforeFormat = new LessonFormatting(message, updatedGrid, cellsInArea, cell, null, null, null);
            return new LessonStep(beforeFormat, null, null, false, currentState);
        }
        else {
            SudokuGraph puzzle = new SudokuGraph(currentState.getCandidates());
            currentState.update(puzzle, currentState.getCandidates(), currentState.getCorrectResponse(), currentState.getPattern(), currentState.getStepsCount(), currentState.checkCands());
            Vector<CandsPattern> nakedSingles = HeuristicsAnalyzer.findNakedSingles(puzzle.findAllCandidates(), puzzle.valuesArray());
            for (int i = 0; i < nakedSingles.size(); i++) {
                CandsPattern nakedSingle = nakedSingles.elementAt(i);
                if (nakedSingle.getCells().contains(response.getCellClicked())) {
                    message = "That cell has a naked single, not a hidden single, because it has only one candidate. In the cell you are looking for," +
                            " it is possible to insert at least two different numbers without violating the rules of Sudoku.";
                    LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsInArea, null, null, null, null);
                    return new LessonStep(beforeFormat, null, null, true, currentState);
                }
            }
            if (cellsInArea.contains(response.getCellClicked())) {
                message = "No. All of the possible numbers for that cell occur as candidates in some other cell in this " + currentState.getPattern().getArea().getName() + ". Click on the " +
                        " cell that contains the hidden single";
            }
            else {
                message = "No. You are looking for a hidden single in the highlighted " + currentState.getPattern().getArea().getName() + ". Click on the cell that contains the hidden single.";
            }
            LessonFormatting beforeFormat = new LessonFormatting(message, currentState.getCandidates(), cellsInArea, null, null, null, null);
            return new LessonStep(beforeFormat, null, null, true, currentState);
        }
    }


    private String createNSIntro() {
        String message = "Often, a Sudoku puzzle has a cell whose number can be determined right away. That is, all but one of the numbers have already" +
                " been assigned within that cell's row, column, and region. Such a cell contains a naked single candidate. Click next to continue.";
        return message;
    }

    private String createHSingleIntro() {
        String message = "A candidate that occurs just once in a row, column, or region is a hidden single candidate. It is called hidden" +
                " because usually it is not the only candidate in its cell. Click Next to continue.";
        return message;
    }
}
