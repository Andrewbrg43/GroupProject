package com.example.groupproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {

    //this is just to see what the player did
    public interface OnCellActionListener {
        void onReveal(int row, int col);
        void onFlag(int row, int col);
    }

    // just to have consistent colours
    private static final int[] NUMBER_COLORS = {
            Color.BLACK,
            Color.parseColor("#1976D2"),   // blue
            Color.parseColor("#388E3C"),   // green
            Color.parseColor("#D32F2F"),   // red
            Color.parseColor("#1A237E"),   // darker blue
            Color.parseColor("#880E4F"),   // darker red maroon
            Color.parseColor("#00838F"),   // light blue
            Color.BLACK,                   // black
            Color.GRAY                     // gray
    };

    private Game game;
    private OnCellActionListener listener;
    private GestureDetector gestureDetector;

    // layout of the board
    private float cellSize;
    private float offsetX;
    private float offsetY;

    // the paint objects are created here not on ondraw()
    private final Paint hiddenPaint = new Paint();
    private final Paint revealedPaint = new Paint();
    private final Paint explodedPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint polePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path flagPath = new Path();


    public BoardView(Context context) {
        super(context);
        init(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        hiddenPaint.setColor(Color.parseColor("#90A4AE"));
        hiddenPaint.setStyle(Paint.Style.FILL);

        revealedPaint.setColor(Color.parseColor("#ECEFF1"));
        revealedPaint.setStyle(Paint.Style.FILL);

        explodedPaint.setColor(Color.parseColor("#E53935"));
        explodedPaint.setStyle(Paint.Style.FILL);

        linePaint.setColor(Color.parseColor("#546E7A"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        minePaint.setColor(Color.BLACK);
        minePaint.setStyle(Paint.Style.FILL);

        flagPaint.setColor(Color.parseColor("#D32F2F"));
        flagPaint.setStyle(Paint.Style.FILL);

        polePaint.setColor(Color.BLACK);
        polePaint.setStyle(Paint.Style.STROKE);
        polePaint.setStrokeWidth(4f);

        crossPaint.setColor(Color.parseColor("#D32F2F"));
        crossPaint.setStyle(Paint.Style.STROKE);
        crossPaint.setStrokeWidth(5f);

        // to recognise taps and holds, just did tap for select, hold for mine, not sure what else to do
        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        // needs to return true
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        int[] cell = cellAt(e.getX(), e.getY());
                        if (cell != null && listener != null) {
                            listener.onReveal(cell[0], cell[1]);
                        }
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        int[] cell = cellAt(e.getX(), e.getY());
                        if (cell != null && listener != null) {
                            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            listener.onFlag(cell[0], cell[1]);
                        }
                    }
                });
    }



    public void setGame(Game game) {
        this.game = game;
        computeLayout();
        invalidate();   // call ondraw()
    }

    public void setOnCellActionListener(OnCellActionListener listener) {
        this.listener = listener;
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        computeLayout();
    }


    // generate the layout and make sure its centered, since phone size means different resolutions, its calculates the size of squares
    private void computeLayout() {
        if (game == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        cellSize = Math.min((float) getWidth() / game.getCols(),
                (float) getHeight() / game.getRows());
        offsetX = (getWidth() - cellSize * game.getCols()) / 2f;
        offsetY = (getHeight() - cellSize * game.getRows()) / 2f;
        textPaint.setTextSize(cellSize * 0.6f);
        polePaint.setStrokeWidth(Math.max(2f, cellSize * 0.08f));
        crossPaint.setStrokeWidth(Math.max(2f, cellSize * 0.1f));
    }


    //just for drawing the board
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null || cellSize == 0) {
            return;
        }

        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                drawCell(canvas, game.getCell(r, c), r, c);
            }
        }
    }

    private void drawCell(Canvas canvas, Cell cell, int row, int col) {
        float left = offsetX + col * cellSize;
        float top = offsetY + row * cellSize;
        float right = left + cellSize;
        float bottom = top + cellSize;
        float cx = left + cellSize / 2f;
        float cy = top + cellSize / 2f;

        boolean lost = game.isLost();
        boolean showMine = lost && cell.isMine() && !cell.isFlagged();
        boolean wrongFlag = lost && cell.isFlagged() && !cell.isMine();

        // painting the background
        Paint background;
        if (cell.isExploded()) {
            background = explodedPaint;
        } else if (cell.isRevealed() || showMine) {
            background = revealedPaint;
        } else {
            background = hiddenPaint;
        }
        canvas.drawRect(left, top, right, bottom, background);

        //creating a border
        canvas.drawRect(left, top, right, bottom, linePaint);

        // contents of cell
        if (cell.isRevealed()) {
            int n = cell.getAdjacentMines();
            if (n > 0) {
                textPaint.setColor(NUMBER_COLORS[n]);
                // have to center the text
                float baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
                canvas.drawText(String.valueOf(n), cx, baseline, textPaint);
            }
        } else if (showMine || cell.isExploded()) {
            canvas.drawCircle(cx, cy, cellSize * 0.25f, minePaint);
        } else if (cell.isFlagged()) {
            drawFlag(canvas, left, top);
            if (wrongFlag) {
                // when you put mine on a safe cell, it does an X
                canvas.drawLine(left + cellSize * 0.2f, top + cellSize * 0.2f,
                        right - cellSize * 0.2f, bottom - cellSize * 0.2f, crossPaint);
                canvas.drawLine(right - cellSize * 0.2f, top + cellSize * 0.2f,
                        left + cellSize * 0.2f, bottom - cellSize * 0.2f, crossPaint);
            }
        }
    }

    //creating a little flag
    private void drawFlag(Canvas canvas, float left, float top) {
        float poleX = left + cellSize * 0.4f;
        flagPath.reset();
        flagPath.moveTo(poleX, top + cellSize * 0.2f);
        flagPath.lineTo(poleX + cellSize * 0.35f, top + cellSize * 0.35f);
        flagPath.lineTo(poleX, top + cellSize * 0.5f);
        flagPath.close();
        canvas.drawPath(flagPath, flagPaint);
        canvas.drawLine(poleX, top + cellSize * 0.2f, poleX, top + cellSize * 0.8f, polePaint);
    }


    //when you touch it, makes gesturedetector check if its a tap or hold
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }


    // takes coordinates of touch into the board position, returns (row, column)
    private int[] cellAt(float x, float y) {
        if (game == null || cellSize == 0) {
            return null;
        }
        int col = (int) Math.floor((x - offsetX) / cellSize);
        int row = (int) Math.floor((y - offsetY) / cellSize);
        if (row < 0 || row >= game.getRows() || col < 0 || col >= game.getCols()) {
            return null;
        }
        return new int[]{row, col};
    }
}
