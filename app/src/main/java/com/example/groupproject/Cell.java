package com.example.groupproject;


public class Cell {
    private boolean mine;          // true if a mine
    private boolean revealed;      // true if uncovered
    private boolean flagged;       // true if flagged
    private boolean exploded;      // true if touch mine
    private int adjacentMines;     // number of adjacent mines

    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }

    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean revealed) { this.revealed = revealed; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public boolean isExploded() { return exploded; }
    public void setExploded(boolean exploded) { this.exploded = exploded; }

    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
}
