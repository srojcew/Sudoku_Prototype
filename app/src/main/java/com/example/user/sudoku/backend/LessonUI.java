package com.example.user.sudoku.backend;

/*
 * Stephen Rojcewicz
 *
 * Interface between the Lesson GUI and the Lesson backend
 *
 */
public interface LessonUI {
    public LessonStep getNextStep(LessonResponse response, LessonStep step);
    public boolean hasNextStep(LessonStep step);
}
