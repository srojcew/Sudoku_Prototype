package com.example.user.sudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class BoardView extends View {
    private int cellSize, textSize;
    private float textCenteringOffsetX, textCenteringOffsetY;
    private Paint linesPaintThin, linesPaintThick, textPaint, fixedTextPaint;
    private MainActivity mainActivity;
    private static final String TAG = "BoardView";
    private Cell selectedCell;
    private Cell[][] cells;
    private static final int THIN_WIDTH = 5, THICK_WIDTH = 20;
    private static final double TEXT_SCALER = 0.8;


    public BoardView(Context context) {
        super(context);
        init(context);
    }
    public BoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }
    public BoardView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init(context);
    }

    private void init(Context context) {
        mainActivity = (MainActivity) context;
        linesPaintThin = new Paint();
        linesPaintThin.setColor(Color.BLACK);
        linesPaintThin.setStrokeWidth(THIN_WIDTH);
        linesPaintThick = new Paint();
        linesPaintThick.setColor(Color.BLACK);
        linesPaintThick.setStrokeWidth(THICK_WIDTH);
        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        fixedTextPaint = new Paint();
        fixedTextPaint.setColor(Color.BLACK);
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new Cell("", new ArrayList<String>(), false);
            }
        }
        selectCell(0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = (int) Math.floor(Math.min(this.getWidth(), this.getHeight()) / 9.0);
        textSize = (int) Math.floor(cellSize * TEXT_SCALER);
        textPaint.setTextSize(textSize);
        fixedTextPaint.setTextSize(textSize);
        Rect textBounds = new Rect();
        textPaint.getTextBounds("0", 0, 1, textBounds);
        float width = textBounds.width() + textBounds.left;
        textCenteringOffsetX = (cellSize - width) / 2;
        textCenteringOffsetY = (cellSize - textBounds.height()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float sideLength = cellSize * 9;

        for (int row = 0; row < 9; row++) {
            float position = row * cellSize;
            Paint linesPaint = row == 3 || row == 6 ? linesPaintThick : linesPaintThin;
            canvas.drawLine(0, position, sideLength, position, linesPaint);
            canvas.drawLine(position, 0, position, sideLength, linesPaint);
            for (int col = 0; col < 9; col++) {
                float textX = col * cellSize + textCenteringOffsetX;
                float textY = position + cellSize - textCenteringOffsetY;
                canvas.drawText(cells[row][col].getValue(), textX, textY, textPaint);
            }
        }
        canvas.drawLine(0, 9 * cellSize, sideLength, 9 * cellSize, linesPaintThin);
        canvas.drawLine(9 * cellSize, 0, 9 * cellSize, sideLength, linesPaintThin);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        if (selectCell(event.getX(), event.getY())) {
            mainActivity.showNumberChooser();
        }
        return true;
    }

    private boolean selectCell(float actualX, float actualY) {
        int x = getCellX(actualX);
        int y = getCellY(actualY);
        if (x >= 0 && x <= 8 && y >= 0 && y <= 8) {
            selectedX = x;
            selectedY = y;
            return true;
        }
        else {
            return false;
        }
    }

    public void setAllCells(String[] cells) {
        for (int i = 0; i < 81; i++) {
            int row = i / 9;
            int col = i % 9;
            cellContents[row][col] = cells[i];
        }
        invalidate();
    }

    public String[] getAllCells() {
        String[] allCells = new String[81];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                allCells[row * 9 + col] = cellContents[row][col];
            }
        }
        return allCells;
    }

    private int getCellX(float actualX) {
        return (int) Math.floor(actualX / cellSize);
    }
    private int getCellY(float actualY) {
        return (int) Math.floor(actualY / cellSize);
    }

    public void setCell(String number) {
        setTextAt(selectedY, selectedX, number);
    }
    public int getSelectedX() {
        return selectedX;
    }
    public int getSelectedY() {
        return selectedY;
    }


    public String getTextAt(int row, int col) {
        return cellContents[row][col];
    }
    public void setTextAt(int row, int col, String num) {
        cellContents[row][col] = num;
        invalidate();
    }
    public void setFixedTextAt(int row, int col, String num) {
        cells[row][col].setText(num);
        cells[row][col].setFont(cells[row][col].getFont().deriveFont(Font.BOLD));
        cells[row][col].setEditable(false);
        cells[row][col].setBackground(null);
    }

    private class Cell {
        private boolean fixed;
        private String value;
        private ArrayList<String> candidates;

        public Cell(String v, ArrayList<String> c, boolean f) {
            value = v;
            candidates = c;
            fixed = f;
        }

        public boolean isFixed() {
            return fixed;
        }
        public String getValue() {
            return value;
        }
        public ArrayList<String> getCandidates() {
            return candidates;
        }
        public void setFixed(boolean f) {
            fixed = f;
        }
        public void setValue(String v) {
            value = v;
        }
        public void setCandidates(ArrayList<String> c) {
            candidates = c;
        }
    }
}
