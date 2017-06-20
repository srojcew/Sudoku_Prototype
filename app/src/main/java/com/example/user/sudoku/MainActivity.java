package com.example.user.sudoku;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;
import android.graphics.Color;

import com.example.user.sudoku.backend.Backend;
import com.example.user.sudoku.backend.TypeConstants;
import com.example.user.sudoku.backend.HintUI;
import com.example.user.sudoku.backend.HintResponse;

public class MainActivity extends AppCompatActivity implements NumChooserDialogFrag.NumChooserDialogFragListener, DifficultyDialogFrag.DifficultyDialogListener {

    private Stack<GameStateImage> undoStack;
    private Stack<GameStateImage> redoStack;
    private HintUI currentHint;
    private String[][] currentPuzzle;
    private Vector<Integer> mistakeCells;
    private static final String mistakeMessage = "You have made a mistake in the red cells.";
    private static final String solvedMessage = "The puzzle is solved.";

    private BoardView boardView;

    /* User preferences */
    private boolean doCheckCands;
    private boolean doUpdateCands;
    private boolean doNotifyMistakes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        undoStack = new Stack<GameStateImage>();
        redoStack = new Stack<GameStateImage>();
        currentHint = null;
        currentPuzzle = null;
        mistakeCells = new Vector<Integer>();
        doCheckCands = true;
        doUpdateCands = true;
        doNotifyMistakes = true;
        boardView = (BoardView) findViewById(R.id.BoardView);
    }

    protected void showNumberChooser() {
        NumChooserDialogFrag numChooser = new NumChooserDialogFrag();
        numChooser.show(getSupportFragmentManager(), "NumChooserDialogFrag");
    }

    public void numSelected(String number) {
        if (number.equals("0")) {
            notifyRightClickedAt(boardView.getSelectedY(), boardView.getSelectedX());
        }
        else {
            boardView.setCell(number);
            notifyCellChanged(boardView.getSelectedY(), boardView.getSelectedX());
        }
    }

    private void generatePossiblyHard(final int difficulty) {
        String[][] puzzle = Backend.makePuzzle(difficulty);
        if (puzzle == null){
            AlertDialog.Builder continueAlertBuilder = new AlertDialog.Builder(this);
            continueAlertBuilder.setMessage(R.string.confirm_continue_generate);
            continueAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    generatePossiblyHard(difficulty);
                }
            });
            continueAlertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
        }
        else {
            undoStack.push(new GameStateImage(this, "new puzzle", currentPuzzle));
            currentPuzzle = puzzle;
            boardView.clear(); // remove leftover bold text formatting
            boardView.removeHighlighting();
            setNewPuzzleText(puzzle[0]);
        }
    }

    public void difficultySelectedNowGenerate(int difficultyChoice) {
        // user selects difficulty
        //Object[] difficultyOptions = {"Easy", "Medium", "Hard", "Hardest", "User Defined Puzzle"};
        //int difficultyChoice = JOptionPane.showOptionDialog(this, "Please select a level of difficulty, or select User Defined Puzzle to enter your own puzzle.", "Select difficulty", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, difficultyOptions, null);
        int difficulty = TypeConstants.EASY;
        switch (difficultyChoice) {
            case 0: difficulty = TypeConstants.EASY;
                break;
            case 1: difficulty = TypeConstants.MEDIUM;
                break;
            case 2: difficulty = TypeConstants.HARD;
                AlertDialog.Builder confirmHardBuilder = new AlertDialog.Builder(this);
                confirmHardBuilder.setMessage(R.string.diff_confirm_msg);
                confirmHardBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        generatePossiblyHard(TypeConstants.HARD);
                    }
                });
                confirmHardBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
                break;
            case 3:
                difficulty = TypeConstants.HARDEST;
                break;
        }
        generatePossiblyHard(difficulty);
    }

    public void newPuzzle(View view) {
        //TODO: confirm user decision
        /*String[][] puzzAndSolution = Backend.makePuzzle(TypeConstants.EASY);
        boardView.setAllCells(puzzAndSolution[0]);*/
        doAction(ButtonAction.NEW);
    }

    public void solve(View view) {
        doAction(ButtonAction.SOLVE);
    }
    public void undo(View view) {
        doAction(ButtonAction.UNDO);
    }
    public void redo(View view) {
        doAction(ButtonAction.REDO);
    }
    public void applyHint(View view) {
        doAction(ButtonAction.APPLY_HINT);
    }
    public void testSolvable(View view) {
        doAction(ButtonAction.TEST_SOLVABLE);
    }

    private void doAction(ButtonAction action) {
        if (action != ButtonAction.APPLY_HINT) {
            currentHint = null;
        }
        if (action == ButtonAction.UNDO) {
            doUndo();
            updateAfterBoardChange();
            doAutoUpdateCandidates();
            return;
        }
        if (action == ButtonAction.REDO) {
            doRedo();
            updateAfterBoardChange();
            doAutoUpdateCandidates();
            return;
        }
        if (action != ButtonAction.HINT) {
            redoStack.clear();
        }

        if (action == ButtonAction.SOLVE) {
            doSolve();
            updateAfterBoardChange();
        }
        else if (action == ButtonAction.NEW) {
            doGenerate();
            doAutoUpdateCandidates();
        }
        else if (action == ButtonAction.HINT) {
            doHint();
            //doAutoUpdateCandidates();
        }
        else if (action == ButtonAction.APPLY_HINT) {
            doApplyHint();
            doAutoUpdateCandidates();
            updateAfterBoardChange();
        }
        else if (action == ButtonAction.TEST_SOLVABLE) {
            doTestSolvable();
        }
        updateButtons();
    }

    private enum ButtonAction {
        NEW, SOLVE, HINT, UNDO, REDO, APPLY_HINT, TEST_SOLVABLE;
    }

    private String[] getCheckedInput(BoardView bView) {
        String[] boardArray = new String[81];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardArray[i * 9 + j] = bView.getTextAt(i, j);
            }
        }
        return boardArray;
    }

    private String[] getCandidatesText() {
        String[] candidatesText = new String[81];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                candidatesText[9 * i + j] = boardView.getCandidatesTextAt(i, j);
            }
        }
        return candidatesText;
    }

    private void doUndo() {
        if (!undoStack.isEmpty()) {
            GameStateImage state = undoStack.pop();
            redoStack.push(new GameStateImage(this, state.getDescription(), currentPuzzle));
            currentPuzzle = state.getCurrentPuzzle();
            currentHint = null;
            boardView.clear();
            boardView.removeHighlighting();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (state.boardIsFixedAt(i, j)) {
                        boardView.setFixedTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    else {
                        boardView.setTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    if (state.candidatesIsFixedAt(i, j)) {
                        boardView.setFixedCandidatesTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                    else {
                        boardView.setCandidatesTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                }
            }
        }
        updateButtons();
        formatCandidatesText();
    }

    private void doRedo() {
        if (!redoStack.isEmpty()) {
            GameStateImage state = redoStack.pop();
            undoStack.push(new GameStateImage(this, state.getDescription(), currentPuzzle));
            currentPuzzle = state.getCurrentPuzzle();
            currentHint = null;
            boardView.clear();
            boardView.removeHighlighting();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (state.boardIsFixedAt(i, j)) {
                        boardView.setFixedTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    else {
                        boardView.setTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    if (state.candidatesIsFixedAt(i, j)) {
                        boardView.setFixedCandidatesTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                    else {
                        boardView.setCandidatesTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                }
            }
        }
        updateButtons();
        formatCandidatesText();
    }

    private void doSolve() {
        undoStack.push(new GameStateImage(this, "solve", currentPuzzle));
        String[] solvedBoard;
        String[] boardArray = getCheckedInput(boardView);
        solvedBoard = Backend.findSolution(boardArray);
        if (solvedBoard == null) {
            setBoardText(currentPuzzle[1]);
            return;
        }
        setBoardText(solvedBoard);
    }

    private void doGenerate() {
        DifficultyDialogFrag difficultyChooser = new DifficultyDialogFrag();
        difficultyChooser.show(getSupportFragmentManager(), "DifficultyChooserDialogFrag");
    }
    private void doAutoUpdateCandidates() {
        if (doUpdateCands) {
            updateCandidates();
        }
    }

    /**
     * modifies the candidates so that they reflect the current committed numbers. Does not add to
     * a candidate list if that list contains the correct number
     */
    private void updateCandidates() {
        if (currentPuzzle != null) {
            String[] boardArray = getCheckedInput(boardView);

            String[] allCalcCandidates = Backend.findCandidates(boardArray);
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    String calcCands = allCalcCandidates[9 * i + j];
                    boardView.setCandidatesTextAt(i, j, allCalcCandidates[9 * i + j]);
                }
            }
        }
    }

    private void doHint() {
        createHint(-1, -1);
    }

    private void createHint(int row, int col) {
        if (Backend.findSolution(getCheckedInput(boardView)) == null) {
            markIncorrectCells();
            Toast.makeText(getApplicationContext(), "You have entered an incorrect number in the red cells", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isSolved()) {
            boardView.removeHighlighting();
            String[] candidates = getCandidates();
            HintUI hint;
            if (row >= 0 && row <= 8 && col >= 0 && col <= 8) {
                hint = Backend.hintAt(row, col, candidates, getCheckedInput(boardView));
            }
            else {
                hint = Backend.hint(candidates, getCheckedInput(boardView));
            }
            Vector<Integer> affectedCells = hint.getAffectedCells();
            if (affectedCells != null) {
                currentHint = hint;
                for (int i = 0; i < affectedCells.size(); i++) {
                    boardView.highlightAt(affectedCells.elementAt(i) / 9, affectedCells.elementAt(i) % 9);
                }
                Toast.makeText(getApplicationContext(), "Hint: "  + hint.getMessage(), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), hint.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private boolean isSolved() {
        String[] currentBoard = boardView.getAllCells();
        return Arrays.equals(currentBoard, currentPuzzle[1]);
    }

    private void doApplyHint() {
        undoStack.push(new GameStateImage(this, "apply hint", currentPuzzle));
        String[] candidates = getCandidates();
        String[] committedNums = getCheckedInput(boardView);
        HintResponse hintResponse = Backend.applyHint(currentHint, candidates, committedNums);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!boardView.getTextAt(i, j).equals(hintResponse.getCommittedNums()[9 * i + j])) {
                    boardView.setTextAt(i, j, hintResponse.getCommittedNums()[9 * i + j]);
                }
                if (!boardView.getCandidatesTextAt(i, j).equals(hintResponse.getCandidates()[9 * i + j])) {
                    boardView.setCandidatesTextAt(i, j, hintResponse.getCandidates()[9 * i + j]);
                }
                formatCandidatesText(i, j);
            }
        }
    }

    private void doTestSolvable() {
        boardView.removeHighlighting();
        currentHint = null;
        if (!checkBoardForMistakes()) {
            Toast.makeText(getApplicationContext(), "Your entries are correct", Toast.LENGTH_SHORT);
            //board.removeHighlighting(Color.RED); // red highlighting should already be removed
        }
    }
    /**
     * marks incorrect cells in red
     * @return true if a mistake was found
     */
    private boolean checkBoardForMistakes() {
        if (Backend.findSolution(getCheckedInput(boardView)) == null) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    checkBoardForMistakesAt(i, j);
                }
            }
            return true;
        }
        return false;
    }
    private boolean checkBoardForMistakesAt(int row, int col) {
        String[] puzzle = getCheckedInput(boardView);
        String[] solvedPuzzle = currentPuzzle[1];
        if (!(puzzle[9 * row + col].equals("") || puzzle[9 * row + col].equals(solvedPuzzle[9 * row + col]))) {
            Toast.makeText(getApplicationContext(), mistakeMessage, Toast.LENGTH_LONG);
            boardView.highlightAt(row, col, Color.RED);
            mistakeCells.add(9 * row + col); // TODO: remove this line?
            return true;
        }
        return false;
    }
    private void markIncorrectCells() {
        String[] puzzle = getCheckedInput(boardView);
        String[] solvedPuzzle = currentPuzzle[1];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!(puzzle[9 * i + j].equals("") || puzzle[9 * i + j].equals(solvedPuzzle[9 * i + j]))) {
                    boardView.highlightAt(i, j, Color.RED);
                    mistakeCells.add(9 * i + j);
                }
            }
        }
    }
    private void setNewPuzzleText(String[] boardText) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!boardText[9 * i + j].equals("")) {
                    boardView.setFixedTextAt(i, j, boardText[9 * i + j]);
                }
            }
        }
    }
    private void setBoardText(String[] boardText) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardView.setTextAt(i, j, boardText[9 * i + j]);
            }
        }
    }
    public void notifyRightClickedAt(int row, int col) {
        Button hintButton = (Button) findViewById(R.id.HintButton);
        if (hintButton.isEnabled() && !isSolved()) {
            createHint(row, col);
            updateButtons();
        }
    }
    /**
     * Updates current hint, calls updateAfterBoardCellChange() for each cell, and checks board for mistakes if doNotifyMistakes is true
     */
    private void updateAfterBoardChange() {
        updateHint();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                updateAfterBoardCellChange(i, j, false);
            }
        }
        if (doNotifyMistakes) {
            checkBoardForMistakes();
        }
    }
    /**
     * updates current hint if checkHint is true, checks for corrected mistakes, checks if puzzle is solved, and formats candidates text
     * @param row
     * @param col
     */
    private void updateAfterBoardCellChange(int row, int col, boolean checkHint) {
        if (currentPuzzle != null) {
            if (checkHint) {
                updateHint();
            }
            String[] puzzle = getCheckedInput(boardView);
            String[] solvedPuz = currentPuzzle[1];
            // check if user has corrected a mistake
            if (!mistakeCells.isEmpty()) {
                for (int i = 0; i < mistakeCells.size(); i++) {
                    int cell = mistakeCells.elementAt(i);
                    if (puzzle[cell].equals("") || puzzle[cell].equals(solvedPuz[cell])) {
                        boardView.removeHighlightingAt(cell / 9, cell % 9);
                        mistakeCells.removeElementAt(i);
                        i--;
                        // reapply hint highlighting
                        Vector<Integer> hintHighlightCells;
                        if (currentHint != null && (hintHighlightCells = currentHint.getAffectedCells()) != null) {
                            if (hintHighlightCells.contains(cell)) {
                                boardView.highlightAt(cell / 9, cell % 9);
                            }
                        }
                    }
                }
            }

            // check if puzzle is solved
            boolean solved = true;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (!puzzle[9 * i + j].equals(solvedPuz[9 * i + j])) {
                        solved = false;
                    }
                }
            }
            if (solved) {
                Toast.makeText(getApplicationContext(), solvedMessage, Toast.LENGTH_LONG).show();
            }

            // format candidates text appropriately
            formatCandidatesText(row, col);
        }
    }
    /**
     * called as soon as the text of a non-candidates board cell changes if that cell currently has focus.
     */
    public void notifyCellChanged(int row, int col) {
        updateAfterBoardCellChange(row, col, true);
        if (doNotifyMistakes) {
            checkBoardForMistakesAt(row, col);
        }
        if (doUpdateCands) {
            updateCandidates();
        }
    }
    /**
     * removes current hint if it is no longer appropriate
     */
    private void updateHint() {
        if (currentHint != null && currentHint.canBeApplied()) {
            // test if hint is still appropriate
            if (!Backend.applyHint(currentHint, getCandidates(), getCheckedInput(boardView)).getChanged()) {
                currentHint = null;
                boardView.removeHighlighting();
            }
        }
        updateButtons();
    }
    private void updateButtons() {
        Button solveButton = (Button) findViewById(R.id.SolveButton);
        Button solvableButton = (Button) findViewById(R.id.TestSolvableButton);
        Button hintButton = (Button) findViewById(R.id.HintButton);
        Button applyButton = (Button) findViewById(R.id.ApplyHintButton);
        Button undoButton = (Button) findViewById(R.id.UndoButton);
        Button redoButton = (Button) findViewById(R.id.RedoButton);
        if (currentPuzzle != null) {
            //board.setCellsEditable(true);
            solveButton.setEnabled(true);
            solvableButton.setEnabled(true);
            hintButton.setEnabled(true);
            if (currentHint != null) {
                applyButton.setEnabled(currentHint.canBeApplied());
            }
            else {
                applyButton.setEnabled(false);
            }
        }
        else {
            boardView.setEditableCellsEditable(false);
            solveButton.setEnabled(false);
            solvableButton.setEnabled(false);
            hintButton.setEnabled(false);
            applyButton.setEnabled(false);
        }
        if (undoStack.isEmpty()) {
            undoButton.setEnabled(false);
            undoButton.setText("Undo");
        }
        else {
            undoButton.setEnabled(true);
            undoButton.setText("Undo " + undoStack.peek().getDescription());
        }
        if (redoStack.isEmpty()) {
            redoButton.setEnabled(false);
            redoButton.setText("Redo");
        }
        else {
            redoButton.setEnabled(true);
            redoButton.setText("Redo " + redoStack.peek().getDescription());
        }
    }
    /*-----------------------------------------------------------------------------------------------------------------------*/
	/* User preferences modification handlers */

    void setDoCheckCands(boolean checkC) {
        doCheckCands = checkC;
    }

    void setDoUpdateCands(boolean updateC) {
        doUpdateCands = updateC;
        updateCandidates();
    }

    void setDoNotifyMistakes(boolean notMistks) {
        doNotifyMistakes = notMistks;
    }
}
