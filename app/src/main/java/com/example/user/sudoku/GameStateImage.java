package com.example.user.sudoku;


/*
 * Stephen Rojcewicz
 *
 * Maintains the state of Game Mode. Used by GamePanel for Undo and Redo
 *
 */

public class GameStateImage {
    private String[][] boardCells;
    private String[][] candidatesCells;
    private boolean[][] boardCellsFixed;
    private boolean[][] candidatesCellsFixed;
    private String description;
    private String[][] currentPuzzle;

    public GameStateImage(MainActivity game, String des, String[][] currentPuz) {
        description = des;
        boardCells = new String[9][9];
        candidatesCells = new String[9][9];
        boardCellsFixed = new boolean[9][9];
        candidatesCellsFixed = new boolean[9][9];
        currentPuzzle = copyPuzzle(currentPuz);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boardCells[i][j] = game.getTextAt(i, j);
                candidatesCells[i][j] = game.getCandidatesTextAt(i, j);
                boardCellsFixed[i][j] = game.textIsFixedAt(i, j);
                candidatesCellsFixed[i][j] = game.candidatesTextIsFixedAt(i, j);
            }
        }
    }

    public String getBoardTextAt(int i, int j) {
        return boardCells[i][j];
    }

    public boolean boardIsFixedAt(int i, int j) {
        return boardCellsFixed[i][j];
    }

    public boolean candidatesIsFixedAt(int i, int j) {
        return candidatesCellsFixed[i][j];
    }

    public String getCandidatesTextAt(int i, int j) {
        return candidatesCells[i][j];
    }

    public String getDescription() {
        return description;
    }

    public String[][] getCurrentPuzzle() {
        return currentPuzzle;
    }

    private  String[][] copyPuzzle(String[][] puzzle) {
        if (puzzle == null) {
            return null;
        }
        String[][] copy = new String[2][81];
        for (int i = 0; i < 81; i++) {
            copy[0][i] = new String(puzzle[0][i]);
            copy[1][i] = new String(puzzle[1][i]);
        }
        return copy;
    }
}