package model;

import exception.InvalidCellReferenceException;

public abstract class BoardState {

    protected final int width;
    protected final int height;
    protected CellState[][] cells;

    public BoardState(int width, int height) throws InvalidCellReferenceException {

        if (width < 0 || height < 0) {
            throw new InvalidCellReferenceException("The game state with width: " + width + ", height: " + height + " is not valid.");
        }

        this.width = width;
        this.height = height;
        this.cells = new CellState[width][height];
    }

    public CellState[][] getCells() {
        return cells;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isValidCell(int x, int y) {
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }

    public int countAdjacentMines(int x, int y) {
        int count = 0;


        if (isValidCell(x - 1, y)) {
            if (cells[x - 1][y].isMined()) {
                count++;
            }
        }

        if (isValidCell(x + 1, y)) {
            if (cells[x + 1][y].isMined()) {
                count++;
            }
        }

        if (isValidCell(x, y + 1)) {
            if (cells[x][y + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(x, y - 1)) {
            if (cells[x][y - 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(x - 1, y + 1)) {
            if (cells[x - 1][y + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(x - 1, y - 1)) {
            if (cells[x - 1][y - 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(x + 1, y + 1)) {
            if (cells[x + 1][y + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(x + 1, y - 1)) {
            if (cells[x + 1][y - 1].isMined()) {
                count++;
            }
        }

        return (count);

    }

}
