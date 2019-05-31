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
     * @param row The new starting row of the player's partial state.
     * @param col The new starting col of the player's partial state.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response move(String sessionID, int row, int col);

    //Performs a reveal action for the specified player

    /**
     * Performs a reveal action for the specified session.
     * @param sessionID The session's UUID
     * @param row The row coordinate of the cell to reveal.
     * @param col The col coordinate of the cell to reveal.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response reveal(String sessionID, int row, int col);

    /**
     * Performs a flag action for the specified session.
     * @param sessionID The session's UUID.
     * @param row The row coordinate of the cell to flag.
     * @param col The col coordinate of the cell to flag.
     * @return Returns a JSON formatted string containing the new partial state of the game as data.
     */
    Response flag(String sessionID, int row, int col);

}