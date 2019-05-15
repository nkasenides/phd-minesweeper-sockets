package model;

import java.util.UUID;

public class Session {

    private final String sessionID;
    private final PartialStatePreference partialStatePreference;
    private final String playerName;
    private final String gameID;
    private int positionX;
    private int positionY;

    public Session(PartialStatePreference partialStatePreference, String playerName, String gameID) {
        this.sessionID = UUID.randomUUID().toString();
        this.partialStatePreference = partialStatePreference;
        this.playerName = playerName;
        this.gameID = gameID;
        this.positionX = 0;
        this.positionY = 0;
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

    public String getGameID() {
        return gameID;
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

}
