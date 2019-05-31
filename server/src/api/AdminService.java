package api;

import model.Difficulty;
import model.PartialStatePreference;
import response.Response;

public interface AdminService {

    /**
     * Creates a new game using the specified parameters.
     * @param password Administrator password (Admin access only).
     * @param maxNumOfPlayers The maximum number of players allowed in the game.
     * @param width The total width of the board.
     * @param height The total height of the board.
     * @param difficulty The game's difficulty {@link Difficulty}.
     * @return Returns a JSON formatted string containing the created game's token as data.
     */
    Response createGame(String password, int maxNumOfPlayers, int width, int height, Difficulty difficulty);

    /**
     * Starts a game using its gameToken.
     * @param password Administrator password (admin access only).
     * @param gameToken The game's token.
     * @return Returns JSON formatted string with the status of the game.
     */
    Response startGame(String password, String gameToken);

    /**
     * Retrieves the state of a game using a gameToken.
     * @param password Administrator password (admin access only).
     * @param gameToken The game's token.
     * @return Returns JSON formatted string with the partial board state and game state of the game.
     */
    Response viewGame(String password, String gameToken, int partialStateWidth, int partialStateHeight, int startRow, int startCol);

}
