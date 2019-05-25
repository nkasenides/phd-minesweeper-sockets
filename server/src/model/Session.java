package model;

import java.util.UUID;

public class Session {

    private final String sessionID;
    private final PartialStatePreference partialStatePreference;
    private final String playerName;
    private final String gameToken;
    private int positionX;
    private int positionY;
    private final boolean spectator;
    private int points;

    public Session(PartialStatePreference partialStatePreference, String playerName, String gameToken, boolean spectator) {
        this.sessionID = UUID.randomUUID().toString();
        this.partialStatePreference = partialStatePreference;
        this.playerName = playerName;
        this.gameToken = gameToken;
        this.positionX = 0;
        this.positionY = 0;
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

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public boolean isSpectator() {
        return spectator;
    }
}
