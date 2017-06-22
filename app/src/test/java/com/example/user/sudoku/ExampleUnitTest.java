package com.example.user.sudoku;

import com.example.user.sudoku.backend.SudokuGraph;

import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(MockitoJUnitRunner.class)

public class ExampleUnitTest {
    @Test
    public void SudokuGraph_constructsCorrect() throws Exception {
        Vector<Vector<Integer>> allCandidates = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            Vector<Integer> cellCandidates = new Vector<Integer>();
            allCandidates.add(cellCandidates);
        }
        allCandidates.elementAt(10).clear();
        allCandidates.elementAt(10).add(2);
        SudokuGraph puzzle = new SudokuGraph(allCandidates);

        for (int i = 0; i < 81; i++) {
            //assertEquals(9, puzzle.findCandidatesAt(i).size());
            assertEquals(1, puzzle.findCandidatesAt(10).size());
        }
    }


}