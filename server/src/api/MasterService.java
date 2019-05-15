package api;

import model.Difficulty;
import model.PartialStatePreference;

public interface MasterService {

    /**
     * Retrieves a list of all games.
     * @return Returns a JSON formatted string containing the created games' tokens.
     */
    String listGames();

    /**
     * Allows a player to join a specified game.
     * @param token The token of the game to join.
     * @param playerName The player's name.
     * @param partialStatePreference The partial state preference of the player.
     * @return Returns a JSON formatted string containing the game session's UUID.
     */
    String join(String token, String playerName, PartialStatePreference partialStatePreference);

}
