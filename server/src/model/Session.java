package model;

import java.util.UUID;

public class Session {

    private final String sessionID;
    private final PartialStatePreference partialStatePreference;
    private final String playerName;
    private final String gameToken;
    private int positionCol;
    private int positionRow;
    private final boolean spectator;
    private int points;

    public Session(PartialStatePreference partialStatePreference, String playerName, String gameToken, boolean spectator) {
        this.sessionID = UUID.randomUUID().toString();
        this.partialStatePreference = partialStatePreference;
        this.playerName = playerName;
        this.gameToken = gameToken;
        this.positionCol = 0;
        this.positionRow = 0;
        this.spectator = spectator;
        this.points = 0;
    }

    public int getPoints() {
        return points;
    }

    public void changePoints(int points) {
        this.points += points;
    }

    public String getSessionID() {
        return sessionID;
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGameToken() {
        return gameToken;
    }

    public int getPositionCol() {
        return positionCol;
    }

    public int getPositionRow() {
        return positionRow;
    }

    public void setPositionCol(int positionCol) {
        this.positionCol = positionCol;
    }

    public void setPositionRow(int positionRow) {
        this.positionRow = positionRow;
    }

    public boolean isSpectator() {
        return spectator;
    }
}
