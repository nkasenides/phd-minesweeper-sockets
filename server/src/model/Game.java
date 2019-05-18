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
        for (int x = 0; x < fullBoardState.getCells().length; x++) {
            for (int y = 0; y < fullBoardState.getCells()[x].length; y++) {
                fullBoardState.getCells()[x][y] = new CellState(false);
            }
        }
    }

    private void generateMines() {
        Random random = new Random();
        final int numberOfMines = Math.round(gameSpecification.getWidth() * gameSpecification.getHeight() * gameSpecification.getDifficulty().getMineRatio());
        int generatedMines = 0;
        do {
            int randomX = random.nextInt(gameSpecification.getWidth());
            int randomY = random.nextInt(gameSpecification.getHeight());
            if (!fullBoardState.getCells()[randomX][randomY].isMined()) {
                fullBoardState.getCells()[randomX][randomY].setMined(true);
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
        for (int x = 0; x < fullBoardState.getWidth(); x++) {
            for (int y = 0; y < fullBoardState.getHeight(); y++) {
                if (fullBoardState.getCells()[x][y].isMined() && fullBoardState.getCells()[x][y].getRevealState() == RevealState.FLAGGED) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countMines() {
        int count = 0;
        for (int x = 0; x < fullBoardState.getWidth(); x++) {
            for (int y = 0; y < fullBoardState.getHeight(); y++) {
                if (fullBoardState.getCells()[x][y].isMined()) {
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

            for (int x = 0; x < fullBoardState.getWidth(); x++) {
                for (int y = 0; y < fullBoardState.getHeight(); y++) {

                    if (fullBoardState.getCells()[x][y].isMined() && fullBoardState.getCells()[x][y].getRevealState() == RevealState.REVEALED_MINE) {
                        gameState = GameState.ENDED_LOST;
                        return;
                    }

                    if (fullBoardState.getCells()[x][y].getRevealState() == RevealState.COVERED) {
                        covered++;
                    }

                }
            }

            if (covered == 0 && flaggedMines == totalMines ) {
                gameState = GameState.ENDED_WON;
                return;
            }

            gameState = GameState.STARTED;
        }
    }

    public void revealAll() {
        FullBoardState state = getFullBoardState();
        for (int x = 0; x < state.getWidth(); x++) {
            for (int y = 0; y < state.getHeight(); y++) {
                if (state.getCells()[x][y].isMined()) {
                    state.getCells()[x][y].setRevealState(RevealState.REVEALED_MINE);
                }
                else {
                    int adjacentMines = getFullBoardState().countAdjacentMines(x, y);
                    state.getCells()[x][y].setRevealState(RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines));
                }
            }
        }
    }

    public void reveal(int x, int y) {
        CellState referencedCell = fullBoardState.getCells()[x][y];
        if (referencedCell.getRevealState() == RevealState.COVERED) {
            if (referencedCell.isMined()) {
                referencedCell.setRevealState(RevealState.REVEALED_MINE);
            }
            else {
                int adjacentMines = fullBoardState.countAdjacentMines(x, y);
                if (adjacentMines > 0) {
                    RevealState revealState = RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines);
                    referencedCell.setRevealState(revealState);
                }
                else {

                    //Reveal current cell:
                    referencedCell.setRevealState(RevealState.REVEALED_0);

                    //Scan adjacent cells, recursively:

                    if (fullBoardState.isValidCell(x - 1, y)) {
                        if (!fullBoardState.getCells()[x - 1][y].isMined()) {
                            reveal(x - 1, y);
                        }
                    }

                    if (fullBoardState.isValidCell(x + 1, y)) {
                        if (!fullBoardState.getCells()[x + 1][y].isMined()) {
                            reveal(x + 1, y);
                        }
                    }

                    if (fullBoardState.isValidCell(x, y + 1)) {
                        if (!fullBoardState.getCells()[x][y + 1].isMined()) {
                            reveal(x, y + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(x, y - 1)) {
                        if (!fullBoardState.getCells()[x][y - 1].isMined()) {
                            reveal(x, y - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(x - 1, y + 1)) {
                        if (!fullBoardState.getCells()[x - 1][y + 1].isMined()) {
                            reveal(x - 1, y + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(x - 1, y - 1)) {
                        if (!fullBoardState.getCells()[x - 1][y - 1].isMined()) {
                            reveal(x - 1, y - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(x + 1, y + 1)) {
                        if (!fullBoardState.getCells()[x + 1][y + 1].isMined()) {
                            reveal(x + 1, y + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(x + 1, y - 1)) {
                        if (!fullBoardState.getCells()[x + 1][y - 1].isMined()) {
                            reveal(x + 1, y - 1);
                        }
                    }

                }
            }
        }
        computeGameState();
    }

    public void flag(int x, int y) {
        CellState referencedCell = fullBoardState.getCells()[x][y];
        if (fullBoardState.getCells()[x][y].getRevealState() == RevealState.COVERED) {
            referencedCell.setRevealState(RevealState.FLAGGED);
        }
        else if (fullBoardState.getCells()[x][y].getRevealState() == RevealState.FLAGGED) {
            referencedCell.setRevealState(RevealState.COVERED);
        }
        computeGameState();
    }

}
