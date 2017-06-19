package com.example.user.sudoku;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class BoardView extends View {
    private int cellSize;
    private Paint linesPaint;
    private MainActivity mainActivity;
    private static final String TAG = "BoardView";
    private int selectedX, selectedY;


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
        selectedX = 0;
        selectedY = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = (int) Math.floor(Math.min(this.getWidth(), this.getHeight()) / 9.0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float sideLength = cellSize * 9;

        for (int i = 0; i < 10; i++) {
            float position = i * cellSize;
            canvas.drawLine(0, position, sideLength, position, linesPaint);
            canvas.drawLine(position, 0, position, sideLength, linesPaint);
        }
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
        selectedX = getCellX(actualX) + 1;
        selectedY = getCellY(actualY) + 1;
    }

    private int getCellX(float actualX) {
        return (int) Math.floor(actualX / cellSize);
    }
    private int getCellY(float actualY) {
        return (int) Math.floor(actualY / cellSize);
    }

    public void setNum(int number) {
        Log.d(TAG, "setting cell (" + selectedX + ", " + selectedY + ") to number: " + number);
    }
}
