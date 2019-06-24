package simulation;

import model.Difficulty;
import java.util.ArrayList;

public class SimulationConfig {

    public static final String SIMULATION_CONFIG_DIR = "SimulationConfigs";

    private int maxPlayers;
    private int gameWidth;
    private int gameHeight;
    private int clientPartialStateWidth;
    private int clientPartialStateHeight;
    private int timeInterval;
    private Difficulty difficulty;
    private ArrayList<AddPlayersEvent> addPlayerEvents;

    public SimulationConfig(int maxPlayers, int gameWidth, int gameHeight, int clientPartialStateWidth, int clientPartialStateHeight, int timeInterval, Difficulty difficulty) {
        this.maxPlayers = maxPlayers;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.clientPartialStateWidth = clientPartialStateWidth;
        this.clientPartialStateHeight = clientPartialStateHeight;
        this.timeInterval = timeInterval;
        this.difficulty = difficulty;
        this.addPlayerEvents = new ArrayList<>();
    }

    private SimulationConfig() { }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public void setGameWidth(int gameWidth) {
        this.gameWidth = gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public void setGameHeight(int gameHeight) {
        this.gameHeight = gameHeight;
    }

    public int getClientPartialStateWidth() {
        return clientPartialStateWidth;
    }

    public void setClientPartialStateWidth(int clientPartialStateWidth) {
        this.clientPartialStateWidth = clientPartialStateWidth;
    }

    public int getClientPartialStateHeight() {
        return clientPartialStateHeight;
    }

    public void setClientPartialStateHeight(int clientPartialStateHeight) {
        this.clientPartialStateHeight = clientPartialStateHeight;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public ArrayList<AddPlayersEvent> getEvents() {
        return addPlayerEvents;
    }

    public void setAddPlayerEvents(ArrayList<AddPlayersEvent> addPlayerEvents) {
        this.addPlayerEvents = addPlayerEvents;
    }

}
