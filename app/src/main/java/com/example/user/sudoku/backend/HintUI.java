package com.example.user.sudoku.backend;

import java.util.Vector;

/*
 * Stephen Rojcewicz
 *
 * GamePanel implements this interface to display hints
 *
 */

public interface HintUI {
    public String getMessage();
    public Vector<Integer> getAffectedCells();
    public boolean canBeApplied();
}