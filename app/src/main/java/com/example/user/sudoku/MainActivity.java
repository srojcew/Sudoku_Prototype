package com.example.user.sudoku;

import android.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity implements NumChooserDialogFrag.NumChooserDialogFragListener {

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

    public void newPuzzle(View view) {
        //TODO: confirm user decision
        String[][] puzzAndSolution = Backend.makePuzzle(TypeConstants.EASY);
        boardView.setAllCells(puzzAndSolution[0]);
    }

    public void solve(View view) {
        currentHint = null;
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
            board.removeHighlighting();
            candidatesBoard.clear();
            candidatesBoard.removeHighlighting();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (state.boardIsFixedAt(i, j)) {
                        board.setFixedTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    else {
                        board.setTextAt(i, j, state.getBoardTextAt(i, j));
                    }
                    if (state.candidatesIsFixedAt(i, j)) {
                        candidatesBoard.setFixedTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                    else {
                        candidatesBoard.setCandidatesTextAt(i, j, state.getCandidatesTextAt(i, j));
                    }
                }
            }
        }
        updateButtons();
        formatCandidatesText();
    }
}
