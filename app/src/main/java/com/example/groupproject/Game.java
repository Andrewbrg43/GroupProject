package com.example.groupproject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;


public class Game {

    public static final int PLAYING = 0;
    public static final int WON = 1;
    public static final int LOST = 2;

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Cell[][] cells;
    private final Random random = new Random();

    private boolean minesPlaced = false;
    private int state = PLAYING;
    private int revealedCount = 0;
    private int flagCount = 0;

    public Game(int rows, int cols, int mineCount) {
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
    }


    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public Cell getCell(int row, int col) { return cells[row][col]; }

    public boolean isWon() { return state == WON; }
    public boolean isLost() { return state == LOST; }

    //true after selected first tile, generates mines after so you never immediately lose
    public boolean isStarted() { return minesPlaced; }

    //showing how many mines are left, at least how many the player thinks
    public int getMinesLeft() { return mineCount - flagCount; }



    //uncovers a cell unless game is over or its flagged
    public void reveal(int row, int col) {
        if (state != PLAYING || !inBounds(row, col)) {
            return;
        }
        Cell cell = cells[row][col];
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        if (!minesPlaced) {
            placeMines(row, col);
        }

        if (cell.isMine()) {
            cell.setExploded(true);
            state = LOST;
            return;
        }

        floodReveal(row, col);

        if (revealedCount == rows * cols - mineCount) {
            state = WON;
            flagAllMines();
        }
    }

    //put flag on or take it off
    public void toggleFlag(int row, int col) {
        if (state != PLAYING || !inBounds(row, col)) {
            return;
        }
        Cell cell = cells[row][col];
        if (cell.isRevealed()) {
            return;
        }
        if (cell.isFlagged()) {
            cell.setFlagged(false);
            flagCount--;
        } else {
            cell.setFlagged(true);
            flagCount++;
        }
    }



    private boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }


    //placing the mines
    private void placeMines(int safeRow, int safeCol) {
        List<Integer> candidates = new ArrayList<Integer>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Math.abs(r - safeRow) > 1 || Math.abs(c - safeCol) > 1) {
                    candidates.add(r * cols + c);
                }
            }
        }
        // if the board is too small, considers spaces it wouldn't otherwise
        if (candidates.size() < mineCount) {
            candidates.clear();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r != safeRow || c != safeCol) {
                        candidates.add(r * cols + c);
                    }
                }
            }
        }

        Collections.shuffle(candidates, random);
        for (int i = 0; i < mineCount; i++) {
            int index = candidates.get(i);
            cells[index / cols][index % cols].setMine(true);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c].setAdjacentMines(countNeighbourMines(r, c));
            }
        }
        minesPlaced = true;
    }

    private int countNeighbourMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if ((dr != 0 || dc != 0) && inBounds(nr, nc) && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }


    //reveals all empty cells
    private void floodReveal(int row, int col) {
        Deque<int[]> toVisit = new ArrayDeque<int[]>();
        cells[row][col].setRevealed(true);
        revealedCount++;
        toVisit.push(new int[]{row, col});

        while (!toVisit.isEmpty()) {
            int[] current = toVisit.pop();
            if (cells[current[0]][current[1]].getAdjacentMines() != 0) {
                continue;   // any numbered cell stops this
            }
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = current[0] + dr;
                    int nc = current[1] + dc;
                    if ((dr == 0 && dc == 0) || !inBounds(nr, nc)) {
                        continue;
                    }
                    Cell neighbour = cells[nr][nc];
                    if (neighbour.isRevealed() || neighbour.isFlagged()) {
                        continue;
                    }
                    neighbour.setRevealed(true);
                    revealedCount++;
                    toVisit.push(new int[]{nr, nc});
                }
            }
        }
    }

    // on a win, flags every mine
    private void flagAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()) {
                    cells[r][c].setFlagged(true);
                }
            }
        }
        flagCount = mineCount;
    }
}
