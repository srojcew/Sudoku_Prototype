package com.example.user.sudoku.backend;

import com.example.user.sudoku.backend.heuristics.lessons.LessonState;

/*
 * Struct representing a particular stage/step in a lesson
 */

public class LessonStep {

    private LessonFormatting beforeState;
    private LessonFormatting afterActionState;
    private String actionMessage;
    private boolean requestClick;
    private LessonState state;

    public LessonStep(LessonFormatting beforeGd, LessonFormatting afterGrid, String aMessage, boolean rqstClk, LessonState sta) {
        beforeState = beforeGd;
        afterActionState= afterGrid;
        actionMessage = aMessage;
        requestClick = rqstClk;
        state = sta;
    }

    public LessonStep(LessonStep step) {
        beforeState = new LessonFormatting(step.getBeforeState());
        if (step.getAfterActionState() == null) {
            afterActionState = null;
        }
        else {
            afterActionState = new LessonFormatting(step.getAfterActionState());
        }
        if (step.getActionMessage() == null) {
            actionMessage = null;
        }
        else {
            actionMessage = new String(step.getActionMessage());
        }
        requestClick = step.hasClickRequest();
        state = new LessonState(step.getState());
    }

    public LessonFormatting getBeforeState() {
        return beforeState;
    }

    public LessonFormatting getAfterActionState() {
        return afterActionState;
    }

    public String getActionMessage() {
        if (actionMessage == null) {
            return "Perform Action";
        }
        return actionMessage;
    }

    public boolean hasClickRequest() {
        return requestClick;
    }

    public LessonState getState() {
        return state;
    }
}
