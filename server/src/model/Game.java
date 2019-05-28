package model;

import exception.InvalidCellReferenceException;

import java.util.*;

public class Game {

    private GameSpecification gameSpecification;
    private FullBoardState fullBoardState;
    private GameState gameState;

    public Game(GameSpecification gameSpecification) {
        this.gameSpecification = gameSpecification;
        this.gameState = GameState.NOT_STARTED;
        try {
            fullBoardState = new FullBoardState(gameSpecification.getWidth(), gameSpecification.getHeight());
            initializeMatrix();
            generateMines();
        } catch (InvalidCellReferenceException e) {
            e.printStackTrace();
        }
    }

    public GameSpecification getGameSpecification() {
        return gameSpecification;
    }

    public FullBoardState getFullBoardState() {
        return fullBoardState;
    }

    private void initializeMatrix() {
        for (int row = 0; row < fullBoardState.getCells().length; row++) {
            for (int col = 0; col < fullBoardState.getCells()[row].length; col++) {
                fullBoardState.getCells()[row][col] = new CellState(false);
            }
        }
    }

    private void generateMines() {
        Random random = new Random();
        final int numberOfMines = Math.round(gameSpecification.getWidth() * gameSpecification.getHeight() * gameSpecification.getDifficulty().getMineRatio());
        int generatedMines = 0;
        do {
            int randomRow = random.nextInt(gameSpecification.getHeight());
            int randomCol = random.nextInt(gameSpecification.getWidth());
            if (!fullBoardState.getCells()[randomRow][randomCol].isMined()) {
                fullBoardState.getCells()[randomRow][randomCol].setMined(true);
                generatedMines++;
            }
        } while (generatedMines < numberOfMines);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean start() {
        if (gameState != GameState.STARTED) {
            this.gameState = GameState.STARTED;
            return true;
        }
        return false;
    }

    private int countFlaggedMines() {
        int count = 0;
        for (int row = 0; row < fullBoardState.getHeight(); row++) {
            for (int col = 0; col < fullBoardState.getWidth(); col++) {
                if (fullBoardState.getCells()[row][col].isMined() && fullBoardState.getCells()[row][col].getRevealState() == RevealState.FLAGGED) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countMines() {
        int count = 0;
        for (int row = 0; row < fullBoardState.getHeight(); row++) {
            for (int col = 0; col < fullBoardState.getWidth(); col++) {
                if (fullBoardState.getCells()[row][col].isMined()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void computeGameState() {

        if (gameState == GameState.STARTED) {
            int covered = 0;
            final int flaggedMines = countFlaggedMines();
            final int totalMines = countMines();

            for (int row = 0; row < fullBoardState.getHeight(); row++) {
                for (int col = 0; col < fullBoardState.getWidth(); col++) {

                    //IMPORTANT NOTE: Commented out for simulation purposes (to run the simulation for a longer time). This should be uncommented in a "normal" minesweeper game.

//                    if (fullBoardState.getCells()[row][col].isMined() && fullBoardState.getCells()[row][col].getRevealState() == RevealState.REVEALED_MINE) {
//                        gameState = GameState.ENDED_LOST;
//                        return;
//                    }

                    if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
                        covered++;
                    }

                }
            }

            if (covered == 0 && flaggedMines == totalMines ) {
                gameState = GameState.ENDED_WON;
                return;
            }

            //IMPORTANT NOTE: For simulation purposes ONLY! This will end the game as a win once all cells have been revealed:
            if (covered < 1) {
                gameState = GameState.ENDED_WON;
                return;
            }

            gameState = GameState.STARTED;
        }
    }

    public void revealAll() {
        FullBoardState state = getFullBoardState();
        for (int row = 0; row < state.getHeight(); row++) {
            for (int col = 0; col < state.getWidth(); col++) {
                if (state.getCells()[row][col].isMined()) {
                    state.getCells()[row][col].setRevealState(RevealState.REVEALED_MINE);
                }
                else {
                    int adjacentMines = getFullBoardState().countAdjacentMines(row, col);
                    state.getCells()[row][col].setRevealState(RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines));
                }
            }
        }
    }

    public RevealState reveal(int row, int col) {
        CellState referencedCell = fullBoardState.getCells()[row][col];
        if (referencedCell.getRevealState() == RevealState.COVERED) {
            if (referencedCell.isMined()) {
                referencedCell.setRevealState(RevealState.REVEALED_MINE);
                computeGameState();
                return RevealState.REVEALED_MINE;
            }
            else {
                int adjacentMines = fullBoardState.countAdjacentMines(row, col);
                if (adjacentMines > 0) {
                    RevealState revealState = RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines);
                    referencedCell.setRevealState(revealState);
                    computeGameState();
                    return revealState;
                }
                else {

                    //Reveal current cell:
                    referencedCell.setRevealState(RevealState.REVEALED_0);

                    //Scan adjacent cells, recursively:
                    if (fullBoardState.isValidCell(row - 1, col)) {
                        if (!fullBoardState.getCells()[row - 1][col].isMined()) {
                            reveal(row - 1, col);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col)) {
                        if (!fullBoardState.getCells()[row + 1][col].isMined()) {
                            reveal(row + 1, col);
                        }
                    }

                    if (fullBoardState.isValidCell(row, col + 1)) {
                        if (!fullBoardState.getCells()[row][col + 1].isMined()) {
                            reveal(row, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row, col - 1)) {
                        if (!fullBoardState.getCells()[row][col - 1].isMined()) {
                            reveal(row, col - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row - 1, col + 1)) {
                        if (!fullBoardState.getCells()[row - 1][col + 1].isMined()) {
                            reveal(row - 1, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row - 1, col - 1)) {
                        if (!fullBoardState.getCells()[row - 1][col - 1].isMined()) {
                            reveal(row - 1, col - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col + 1)) {
                        if (!fullBoardState.getCells()[row + 1][col + 1].isMined()) {
                            reveal(row + 1, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col - 1)) {
                        if (!fullBoardState.getCells()[row + 1][col - 1].isMined()) {
                            reveal(row + 1, col - 1);
                        }
                    }
                    computeGameState();
                    return RevealState.REVEALED_0;

                }
            }
        }
        computeGameState();
        return referencedCell.getRevealState();
    }

    public void flag(int row, int col) {
        CellState referencedCell = fullBoardState.getCells()[row][col];
        if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
            referencedCell.setRevealState(RevealState.FLAGGED);
        }
        else if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.FLAGGED) {
            referencedCell.setRevealState(RevealState.COVERED);
        }
        computeGameState();
    }

}
