package api;

import model.Difficulty;
import model.PartialStatePreference;
import response.Response;

public interface MasterService {

    /**
     * Retrieves a list of all games.
     * @return Returns a JSON formatted string containing the created games' tokens.
     */
    Response listGames();

    /**
     * Allows a player to join a specified game.
     * @param token The token of the game to join.
     * @param playerName The player's name.
     * @param partialStateWidth width of the preferred partial state.
     * @param partialStateHeight height of the preferred partial state.
     * @return Returns a JSON formatted string containing the game session's UUID.
     */
    Response join(String token, String playerName, int partialStateWidth, int partialStateHeight);

}
