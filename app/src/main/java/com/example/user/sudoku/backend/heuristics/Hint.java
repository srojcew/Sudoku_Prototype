package com.example.user.sudoku.backend.heuristics;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import com.example.user.sudoku.backend.HintUI;
import com.example.user.sudoku.backend.TypeConstants;
import com.example.user.sudoku.backend.heuristics.lessons.LessonFunctions;

/*
 * Stephen Rojcewicz
 *
 * Represents a hint to be used in Game Mode
 *
 */

public class Hint implements HintUI {

    private String message = null;
    private Vector<Integer> cellsAffected;
    private CandsPattern pattern = null;
    private boolean canBeApplied = false;

    public Hint(String msg) {
        message = msg;
        cellsAffected = new Vector<Integer>();
    }
    /**
     * Parses a Candidates Patterns and creates the corresponding hint
     * @param pn
     */
    public Hint(CandsPattern pn) {
        pattern = pn;
        cellsAffected = new Vector<Integer>();
        createMessage();
    }

    public Hint(String msg, Vector<Integer> cells, boolean cbApplied) {
        message = msg;
        cellsAffected = cells;
        canBeApplied = cbApplied;
    }

    private void createMessage() {
        message = "";
        if (pattern.getType() == TypeConstants.N_SINGLE) {
            nakedSingleMessage();
        }
        else if (pattern.getType() == TypeConstants.H_SINGLE) {
            hiddenSingleMessage();
        }
        else if (pattern.getType() == TypeConstants.N_PAIR) {
            nakedPairMessage();
        }
        else if (pattern.getType() == TypeConstants.N_TRIPLE) {
            nakedTripleMessage();
        }
        else if (pattern.getType() == TypeConstants.H_PAIR) {
            hiddenPairMessage();
        }
        else if (pattern.getType() == TypeConstants.H_TRIPLE) {
            hiddenTripleMessage();
        }
    }

    private void nakedSingleMessage() {
        int cell = pattern.getCells().firstElement();
        message += "The highlighted cell has only one candidate. This cell's value must be " + (pattern.getNums()[0] + 1);
        cellsAffected.add(cell);
    }

    private void hiddenSingleMessage() {
        message += "There is only one place that a " + (pattern.getNums()[0] + 1) + " could go in the highlighted " + pattern.getArea().getName() + ".";
        cellsAffected = pattern.getArea().getCells();
    }

    private void nakedPairMessage() {
        int[] nums = pattern.getNums();
        cellsAffected = pattern.getArea().getCells();
        message += "Naked Pair (" + (nums[0] + 1) + ", " + (nums[1] + 1) + ") in the highlighted " + pattern.getArea().getName() + ". Any " + (nums[0] + 1) + "s " +
                " or " + (nums[1] + 1) + "s can be removed from the cells in this " + pattern.getArea().getName() + " that do not contain the naked pair";
    }

    private void hiddenPairMessage() {
        int[] nums = pattern.getNums();
        cellsAffected = pattern.getArea().getCells();
        message += "Hidden Pair (" + (nums[0] + 1) + ", " + (nums[1] + 1) + ") in the highlighted " + pattern.getArea().getName() + ". Any candidates that " +
                "are not " + (nums[0] + 1) + " or " + (nums[1] + 1) + " can be removed from the two cells in which the pair occurs.";
    }

    private void nakedTripleMessage() {
        int[] nums = pattern.getNums();
        cellsAffected = pattern.getArea().getCells();
        message += "Naked Triple (" + (nums[0] + 1) + ", " + (nums[1] + 1) + ", " + (nums[2] + 1) + ") in the highlighted " + pattern.getArea().getName() + ". Any " +
                (nums[0] + 1) + "s, " + (nums[1] + 1) + "s, or " + (nums[2] + 1) + "s can be removed from the cells in this " + pattern.getArea().getName() + " " +
                "that do not contain the naked triple.";
    }

    private void hiddenTripleMessage() {
        int[] nums = pattern.getNums();
        cellsAffected = pattern.getArea().getCells();
        message += "Hidden Triple (" + (nums[0] + 1) + ", " + (nums[1] + 1) + ", " + (nums[2] + 1) + ") in the highlighted " + pattern.getArea().getName() +". Any candidates that " +
                "are not " + (nums[0] + 1) + ", " + (nums[1] + 1) + ", or " + (nums[2] + 1) + " can be removed from the three cells in which the triple occurs.";
    }

    public CandsPattern getPattern() {
        return pattern;
    }

    public String getMessage() {
        return message;
    }

    /**
     * cellsAffected is null if the hint does not affect any particular cells
     */
    public Vector<Integer> getAffectedCells() {
        if (cellsAffected.isEmpty()) {
            return null;
        }
        return cellsAffected;
    }

    public boolean canBeApplied() {
        return ((pattern != null) || canBeApplied);
    }
}
