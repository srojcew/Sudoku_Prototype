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
import java.util.Stack;
import java.util.Vector;

import com.example.user.sudoku.backend.Backend;
import com.example.user.sudoku.backend.TypeConstants;
import com.example.user.sudoku.backend.HintUI;

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
            doAction(ButtonAction.HINT);
        }
        else {
            boardView.setCell(number);
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
}
