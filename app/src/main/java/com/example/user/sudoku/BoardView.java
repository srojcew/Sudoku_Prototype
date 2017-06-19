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

public class BoardView extends View {
    private int cellSize, textSize;
    private float textCenteringOffsetX, textCenteringOffsetY;
    private Paint linesPaintThin, linesPaintThick, textPaint;
    private MainActivity mainActivity;
    private static final String TAG = "BoardView";
    private int selectedX, selectedY;
    private Integer[][] cellContents;
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
        textPaint.setColor(Color.BLACK);
        selectCell(0, 0);
        cellContents = new Integer[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cellContents[i][j] = 0;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = (int) Math.floor(Math.min(this.getWidth(), this.getHeight()) / 9.0);
        textSize = (int) Math.floor(cellSize * TEXT_SCALER);
        textPaint.setTextSize(textSize);
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
                canvas.drawText(cellContents[row][col].toString(), textX, textY, textPaint);
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
            Log.d(TAG, "user selected");
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

    private int getCellX(float actualX) {
        return (int) Math.floor(actualX / cellSize);
    }
    private int getCellY(float actualY) {
        return (int) Math.floor(actualY / cellSize);
    }

    public void setNum(int number) {
        Log.d(TAG, "setting cell (" + selectedX + ", " + selectedY + ") to number: " + number);
        cellContents[selectedY][selectedX] = number;
        invalidate();
    }
}
