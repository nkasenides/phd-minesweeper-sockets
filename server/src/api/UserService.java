package api;

import model.Direction;
import response.Response;

public interface UserService {

    /**
     * Allows a player to retrieve the partial state of the game.
     * @param sessionID The session's UUID
     * @return Returns a JSON formatted string containing the game's partial state as data.
     */
    Response getPartialState(String sessionID);

    /**
     * Performs a move (shift of partial state position) for a given player to a given cell.
     * @param sessionID The session's UUID
     * @param x The new starting x of the player's partial state.
     * @param y The new starting y of the player's partial state.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response move(String sessionID, int x, int y);

    //Performs a reveal action for the specified player

    /**
     * Performs a reveal action for the specified session.
     * @param sessionID The session's UUID
     * @param x The x coordinate of the cell to reveal.
     * @param y The y coordinate of the cell to reveal.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response reveal(String sessionID, int x, int y);

    /**
     * Performs a flag action for the specified session.
     * @param sessionID The session's UUID.
     * @param x The x coordinate of the cell to flag.
     * @param y The y coordinate of the cell to flag.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response flag(String sessionID, int x, int y);

}