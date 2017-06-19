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

import com.example.user.sudoku.backend.Backend;
import com.example.user.sudoku.backend.TypeConstants;

public class MainActivity extends AppCompatActivity implements NumChooserDialogFrag.NumChooserDialogFragListener {
    
    private JButton showButton;
    private JButton hintButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton acceptButton;
    private JButton defCancelButton;
    private JButton applyButton;
    private JPanel buttonsPanel;
    private JPanel undoRedoPanel;
    private BoardPanel board;
    private CandidatesPanel candidatesBoard;
    private JLabel board1Label;
    private JPanel board2LabelPanel;
    private JPanel messagePanel;
    private JTextArea messageArea;
    private Stack<GameStateImage> undoStack;
    private Stack<GameStateImage> redoStack;
    private HintUI currentHint;
    private JFrame defFrame;
    private JPanel defPanel;
    private JTextArea defMessageArea;
    private BoardPanel defBoard;
    private JButton solvableButton;
    private String[][] currentPuzzle;
    private Vector<Integer> mistakeCells;
    private static final String mistakeMessage = "You have made a mistake in the red cells.";
    private static final String mistakeCandsMessage = "Your candidate list in the red cell does not contain the correct number.";
    private static final String solvedMessage = "The puzzle is solved.";

    /* User preferences */
    private boolean doCheckCands;
    private boolean doUpdateCands;
    private boolean doNotifyMistakes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void showNumberChooser() {
        NumChooserDialogFrag numChooser = new NumChooserDialogFrag();
        numChooser.show(getSupportFragmentManager(), "NumChooserDialogFrag");
    }

    public void numSelected(String number) {
        BoardView boardView = (BoardView) findViewById(R.id.BoardView);
        boardView.setCell(number);
    }

    public void newPuzzle(View view) {
        //TODO: confirm user decision
        String[][] puzzAndSolution = Backend.makePuzzle(TypeConstants.EASY);
        BoardView boardView = (BoardView) findViewById(R.id.BoardView);
        boardView.setAllCells(puzzAndSolution[0]);
    }
}
