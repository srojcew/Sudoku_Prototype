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
    //TODO: undo redo for every move
    //TODO: no "_" for empty cells
    private int cellSize, valueTextSize, candidatesTextSize;
    //private float textCenteringOffsetX, textCenteringOffsetY;
    private Paint linesPaintThin, linesPaintThick, editableTextPaint, fixedTextPaint, cellBackgroundPaint, candidatesPaint, selectedCellPaint, numberChooserPaint;
    private MainActivity mainActivity;
    private static final String TAG = "BoardView";
    private Cell selectedCell;
    private Cell[][] cells;
    private static final int THIN_WIDTH = 5, THICK_WIDTH = 20;
    private static final double VALUE_TEXT_SCALER = 0.8, CANDIDATES_TEXT_SCALER = 0.25, CHOOSER_TEXT_SCALER = 1.2;
    private boolean solved;

    //private CellValueChoices cellValueChoices = null;


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
        cellBackgroundPaint = new Paint();
        cellBackgroundPaint.setColor(Color.WHITE);
        linesPaintThin = new Paint();
        linesPaintThin.setColor(Color.BLACK);
        linesPaintThin.setStrokeWidth(THIN_WIDTH);
        linesPaintThick = new Paint();
        linesPaintThick.setColor(Color.BLACK);
        linesPaintThick.setStrokeWidth(THICK_WIDTH);
        editableTextPaint = new Paint();
        editableTextPaint.setColor(Color.BLUE);
        candidatesPaint = new Paint();
        candidatesPaint.setColor(Color.BLACK);
        fixedTextPaint = new Paint();
        fixedTextPaint.setColor(Color.BLACK);
        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.LTGRAY);
        numberChooserPaint = new Paint();
        numberChooserPaint.setColor(Color.GREEN);
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new Cell(i, j, "", "", true, Color.WHITE);
            }
        }
        //selectCell(0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;


        width = widthSize;

        height = Math.min(widthSize, heightSize);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = (int) Math.floor(Math.min(this.getWidth(), this.getHeight()) / 9.0);
        valueTextSize = (int) Math.floor(cellSize * VALUE_TEXT_SCALER);
        candidatesTextSize = (int) Math.floor(cellSize * CANDIDATES_TEXT_SCALER);
        editableTextPaint.setTextSize(valueTextSize);
        fixedTextPaint.setTextSize(valueTextSize);
        candidatesPaint.setTextSize(candidatesTextSize);
        /*Rect textBounds = new Rect();
        editableTextPaint.getTextBounds("0", 0, 1, textBounds);
        float width = textBounds.width() + textBounds.left;
        textCenteringOffsetX = (cellSize - width) / 2;
        textCenteringOffsetY = (cellSize - textBounds.height()) / 2;*/
        numberChooserPaint.setTextSize((float) (valueTextSize * CHOOSER_TEXT_SCALER));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float sideLength = cellSize * 9;

        for (int row = 0; row < 9; row++) {
            float position = row * cellSize;
            for (int col = 0; col < 9; col++) {
                cellBackgroundPaint.setColor(cells[row][col].getBackHighlightColor());
                canvas.drawRect(col * cellSize, position, col * cellSize + cellSize, position + cellSize, cellBackgroundPaint);
                Paint cellValuePaint = cells[row][col].isFixed()? fixedTextPaint : editableTextPaint;
                drawCenteredTextInCell(cells[row][col].getValue(), row, col, cellValuePaint, canvas);

                // draw candidate numbers
                String candidates = cells[row][col].getCandidates();
                if (cells[row][col].getValue().isEmpty() && !candidates.isEmpty()) {
                    drawCenteredTextInCell(candidates, row, col, candidatesPaint, canvas);
                }
            }

        }
        for (int i = 0; i < 9; i++) {
            float position = i * cellSize;
            Paint linesPaint = i == 3 || i == 6 ? linesPaintThick : linesPaintThin;
            canvas.drawLine(0, position, sideLength, position, linesPaint);
            canvas.drawLine(position, 0, position, sideLength, linesPaint);
        }
        canvas.drawLine(0, 9 * cellSize, sideLength, 9 * cellSize, linesPaintThin);
        canvas.drawLine(9 * cellSize, 0, 9 * cellSize, sideLength, linesPaintThin);

        // show number chooser
        /*if (cellValueChoices != null) {
            int row = cellValueChoices.getCell().getRow();
            int col = cellValueChoices.getCell().getColumn();

            //highlight cell
            canvas.drawRect(col * cellSize, row * cellSize, col * cellSize + cellSize, row * cellSize + cellSize, selectedCellPaint);
            cellValueChoices.drawChoices(canvas);*/

            /*String candidates = cells[row][col].getCandidates();

            float offset = cellSize / 2;
            for (int i = 0; i < candidates.length(); i++) {
                if (!("" + candidates.charAt(i)).equals(currentValue)) {
                    float rowOffset = (-1 + i / 3) * offset  - textCenteringOffsetY;
                    float colOffset = (-1 + i % 3) * offset  + textCenteringOffsetX;
                    canvas.drawText("" + candidates.charAt(i), col * cellSize + colOffset, row * cellSize + cellSize + rowOffset, numberChooserPaint);
                }
            }
        }*/
    }

    private float findStartXToCenterAt(String text, float x, Paint textPaint) {
        if (text.isEmpty()) {
            return x;
        }
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float width = textBounds.width() + textBounds.left;
        return x - width / 2;
    }
    private float findStartYToCenterAt(String text, float y, Paint textPaint) {
        if (text.isEmpty()) {
            return y;
        }
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        return y + textBounds.height() / 2;
    }
    private void drawCenteredTextInCell(String text, int row, int col, Paint textPaint, Canvas canvas) {
        drawCenteredText(text, col * cellSize + cellSize / 2, row * cellSize + cellSize / 2, textPaint, canvas);
    }
    private void drawCenteredText(String text, float x, float y, Paint textPaint, Canvas canvas) {
        float startX = findStartXToCenterAt(text, x, textPaint);
        float startY = findStartYToCenterAt(text, y, textPaint);
        canvas.drawText(text, startX, startY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        if (selectedCell == null) {
            int row = getCellY(event.getY());
            int col = getCellX(event.getX());
            if (row >= 0 && row <= 8 && col >= 0 && col <= 8) {
                if (!cells[row][col].isFixed()) {
                    selectCell(row, col);
                    mainActivity.showNumberChooser();
                }
            }
        }



        /*if (cellValueChoices == null) {
            int row = getCellY(event.getY());
            int col = getCellX(event.getX());
            if (row >= 0 && row <= 8 && col >= 0 && col <= 8) {
                if (!cells[row][col].isFixed()) {
                    if (!cells[row][col].getCandidates().isEmpty()) {
                        //selectCell(row, col);
                        cellValueChoices = new CellValueChoices(cells[row][col]);
                    }
                }
            }
        }
        else {
            String selectedValue = cellValueChoices.getValueAtPosition(event.getX(), event.getY());
            if (selectedValue != null) {
                mainActivity.cellValueSet(cellValueChoices.getCell().getRow(), cellValueChoices.getCell().getColumn());
                //setCell(selectedValue);
               // Cell cellToSet = cellValueChoices.getCell();
               // cellValueChoices = null;
               // cellToSet.setValue(selectedValue);
                cellValueChoices.getCell().setValue(selectedValue);
            }
            cellValueChoices = null;
        }*/
        invalidate();
        return true;
    }


    private void selectCell(int row, int col) {
        selectedCell = cells[row][col];
    }


    public String[] getAllCells() {
        String[] allCells = new String[81];
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                allCells[row * 9 + col] = cells[row][col].getValue();
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
        selectedCell.setValue(number);
    }
    public int getSelectedX() {
        return selectedCell.getColumn();
    }
    public int getSelectedY() {
        return selectedCell.getRow();
    }


    public String getTextAt(int row, int col) {
        return cells[row][col].getValue();
    }
    public String getCandidatesTextAt(int row, int col) {
        return cells[row][col].getCandidates();
    }
    public void setCandidatesTextAt(int row, int col, String candidates) {
        cells[row][col].setCandidates(candidates);
    }
    public void setFixedCandidatesTextAt(int row, int col, String candidates) {
        setCandidatesTextAt(row, col, candidates);
    }
    public boolean candidatesTextIsFixedAt(int row, int col) {
        return false;
    }
    public void setTextAt(int row, int col, String num) {
        cells[row][col].setValue(num);
    }
    public void setFixedTextAt(int row, int col, String num) {
        cells[row][col].setFixed(true);
        cells[row][col].setValue(num);
    }
    public boolean textIsFixedAt(int row, int col) {
        return cells[row][col].isFixed();
    }
    public void highlightAt(int row, int col) {
        cells[row][col].setBackHighlightColor(R.color.lightHighlight);
    }
    public void highlightAt(int row, int col, int color) {
        cells[row][col].setBackHighlightColor(color);
    }
    public void setEditableCellsEditable(boolean editable) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!cells[i][j].isFixed()) {
                    cells[i][j].setFixed(!editable);
                }
            }
        }
    }
    public void setAllCellsEditable(boolean editable) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setFixed(!editable);
            }
        }
    }
    public void setEditableAt(int row, int col, boolean editable) {
        cells[row][col].setFixed(!editable);
    }
    public void removeHighlighting() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setBackHighlightColor(Color.WHITE);
            }
        }
    }
    public void removeHighlightingAt(int row, int col) {
        cells[row][col].setBackHighlightColor(Color.WHITE);
    }
    public void removeHighlighting(int color) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getBackHighlightColor() == color) {
                    cells[i][j].setBackHighlightColor(Color.WHITE);
                }
            }
        }
    }
    public void clear() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].clear();
            }
        }
        selectedCell = null;
        //cellValueChoices = null;
    }



    private class Cell {
        private boolean fixed;
        private String value;
        private String candidates;
        private int backHighlightColor;
        private final int row, column;

        public Cell(int r, int col, String v, String c, boolean f, int b) {
            row = r;
            column = col;
            value = v;
            candidates = c;
            fixed = f;
            backHighlightColor = b;
        }

        public void clear() {
            value = "";
            fixed = false;
            candidates = "";
            backHighlightColor = Color.WHITE;
        }

        public boolean isFixed() {
            return fixed;
        }
        public String getValue() {
            return value;
        }
        public String getCandidates() {
            return candidates;
        }

        public int getBackHighlightColor() {
            return backHighlightColor;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public void setBackHighlightColor(int b) {
            backHighlightColor = b;
            invalidate();
        }
        public void setFixed(boolean f) {
            fixed = f;
            invalidate();
        }
        public void setValue(String v) {
            value = v;
            invalidate();
        }
        public void setCandidates(String c) {
            candidates = c;
            invalidate();
        }
    }

    /*private class CellValueChoices {
        private final Cell cell;
        private ArrayList<CandidateValue> candidateValues;
        private final float drawRadius = cellSize * .7f;

        public CellValueChoices (Cell c) {
            cell = c;
            candidateValues = new ArrayList<CandidateValue>();
            float centerX = cell.getColumn() * cellSize + cellSize / 2;
            float centerY = cell.getRow() * cellSize + cellSize / 2;



            //draw the candidate values in a circle around the cell
            double arcSpacing = (2 * Math.PI) / (c.getCandidates().length() + 1);
            double angle = 0;
            candidateValues.add(new CandidateValue("", centerX + drawRadius, centerY, numberChooserPaint));
            angle += arcSpacing;
            for (int i = 0; i < c.getCandidates().length(); i++) {
                //if (!(c.getCandidates().charAt(i) + "").equals(cell.getValue())) {
                    double x = centerX + drawRadius * Math.cos(angle);
                    double y = centerY + drawRadius * Math.sin(angle);
                    candidateValues.add(new CandidateValue(c.getCandidates().charAt(i) + "", (float) x, (float) y, numberChooserPaint));
                    angle += arcSpacing;
                //}
            }
        }

        public Cell getCell() {
            return cell;
        }

        public String getValueAtPosition(float x, float y) {
            for (CandidateValue candidateValue : candidateValues) {
                Rect boundingBox = candidateValue.boundingBox;
                if (boundingBox.contains((int) x, (int) y)) {
                    return candidateValue.value;
                }
            }
            return null;
        }

        private Rect createRectCenteredAt(float x, float y, float width, float height) {
            return new Rect((int) (x - width / 2), (int) (y - height / 2), (int) (x + width / 2), (int) (y + height / 2));
        }

        public void drawChoices(Canvas canvas) {
            for (CandidateValue candidateValue : candidateValues) {
                candidateValue.draw(canvas);
            }
        }

        private class CandidateValue {
            private String value, textToDraw;
            private Rect boundingBox;
            private float centeredX, centeredY;
            private Paint textPaint;

            public CandidateValue(String v, float centeredX, float centeredY, Paint textPaint) {
                this.centeredX = centeredX;
                this.centeredY = centeredY;
                this.textPaint = textPaint;
                value = v;
                textToDraw = v.equals("") ? "[ ]" : v;
                Rect stringSizeRect = new Rect();
                textPaint.getTextBounds(textToDraw, 0, textToDraw.length(), stringSizeRect);
                boundingBox = createRectCenteredAt(centeredX, centeredY, stringSizeRect.width(), stringSizeRect.height());
            }

            public void draw(Canvas canvas) {

                drawCenteredText(textToDraw, centeredX, centeredY, textPaint, canvas);
            }
        }
    }*/
}
