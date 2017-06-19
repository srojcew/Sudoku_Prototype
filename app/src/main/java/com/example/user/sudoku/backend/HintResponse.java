package com.example.user.sudoku.backend;
import java.util.Vector;

/*
 * Stephen Rojcewicz
 *
 * The result of applying a hint. If the hint did not result in modification, then changed is false
 *
 */

public class HintResponse {

    private Vector<Vector<Integer>> candidates;
    private int[] committedNums;
    private boolean changed;

    public HintResponse(Vector<Vector<Integer>> cands, int[] commNs, boolean chngd) {
        candidates = cands;
        committedNums = commNs;
        changed = chngd;
    }

    public String[] getCandidates() {
        return Backend.candidatesIntToString(candidates);
    }
    public String[] getCommittedNums() {
        return Backend.boardIntToString(committedNums);
    }
    public boolean getChanged() {
        return changed;
    }

}
