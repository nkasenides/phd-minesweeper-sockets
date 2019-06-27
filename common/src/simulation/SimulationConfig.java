package simulation;

import model.Difficulty;
import java.util.ArrayList;

public class SimulationConfig {

    public static final String SIMULATION_CONFIG_DIR = "SimulationConfigs";

    private int maxPlayers;
    private int gameWidth;
    private int gameHeight;
    private int playerPartialStateWidth;
    private int playerPartialStateHeight;
    private int adminPartialStateWidth;
    private int adminPartialStateHeight;
    private int timeInterval;
    private Difficulty difficulty;
    private ArrayList<AddPlayersEvent> addPlayerEvents;

    public SimulationConfig(int maxPlayers, int gameWidth, int gameHeight, int playerPartialStateWidth, int playerPartialStateHeight,
                            int adminPartialStateWidth, int adminPartialStateHeight, int timeInterval, Difficulty difficulty) {
        this.maxPlayers = maxPlayers;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.playerPartialStateWidth = playerPartialStateWidth;
        this.playerPartialStateHeight = playerPartialStateHeight;
        this.adminPartialStateWidth = adminPartialStateWidth;
        this.adminPartialStateHeight = adminPartialStateHeight;
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

    public int getPlayerPartialStateWidth() {
        return playerPartialStateWidth;
    }

    public void setPlayerPartialStateWidth(int playerPartialStateWidth) {
        this.playerPartialStateWidth = playerPartialStateWidth;
    }

    public int getPlayerPartialStateHeight() {
        return playerPartialStateHeight;
    }

    public void setPlayerPartialStateHeight(int playerPartialStateHeight) {
        this.playerPartialStateHeight = playerPartialStateHeight;
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

    public int getAdminPartialStateWidth() {
        return adminPartialStateWidth;
    }

    public int getAdminPartialStateHeight() {
        return adminPartialStateHeight;
    }

    public void setAdminPartialStateWidth(int adminPartialStateWidth) {
        this.adminPartialStateWidth = adminPartialStateWidth;
    }

    public void setAdminPartialStateHeight(int adminPartialStateHeight) {
        this.adminPartialStateHeight = adminPartialStateHeight;
    }

}
