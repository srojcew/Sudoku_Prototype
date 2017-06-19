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
    private Paint linesPaint, textPaint;
    private MainActivity mainActivity;
    private static final String TAG = "BoardView";
    private int selectedX, selectedY;
    private Integer[][] cellContents;


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
        linesPaint = new Paint();
        linesPaint.setColor(Color.BLACK);
        linesPaint.setStrokeWidth(2);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        selectCell(0, 0);
        cellContents = new Integer[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cellContents[i][j] = 5;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = (int) Math.floor(Math.min(this.getWidth(), this.getHeight()) / 9.0);
        textSize = cellSize;
        textPaint.setTextSize(textSize);
        Rect textBounds = new Rect();
        textPaint.getTextBounds("0", 0, 1, textBounds);
        textCenteringOffsetX = (cellSize - textBounds.width()) / 2;
        textCenteringOffsetY = (cellSize - textBounds.height()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float sideLength = cellSize * 9;

        for (int row = 0; row < 9; row++) {
            float position = row * cellSize;
            canvas.drawLine(0, position, sideLength, position, linesPaint);
            canvas.drawLine(position, 0, position, sideLength, linesPaint);
            for (int col = 0; col < 9; col++) {
                float textX = col * cellSize + textCenteringOffsetX;
                float textY = position + cellSize - textCenteringOffsetY;
                canvas.drawText(cellContents[row][col].toString(), textX, textY, textPaint);
            }
        }
        canvas.drawLine(0, 9 * cellSize, sideLength, 9 * cellSize, linesPaint);
        canvas.drawLine(9 * cellSize, 0, 9 * cellSize, sideLength, linesPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event);
        }
        selectCell(event.getX(), event.getY());
        Log.d(TAG, "user selected");
        mainActivity.showNumberChooser();
        return true;
    }

    private void selectCell(float actualX, float actualY) {
        selectedX = getCellX(actualX);
        selectedY = getCellY(actualY);
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
