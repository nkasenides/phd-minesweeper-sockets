package services;

import api.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datastore.Datastore;
import exception.InvalidCellReferenceException;
import model.*;
import response.ErrorResponse;
import response.Response;
import response.SuccessResponse;
import server.Server;

public class LocalUserService implements UserService {

    @Override
    public Response getPartialState(String sessionID) {

        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            return new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
        }

        //If session valid, try to get the referenced game:
        else {

            Game referencedGame = Datastore.getGame(referencedSession.getGameToken());

            //If game not found for this session, return error:
            if (referencedGame == null) {
                return new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
            }

            //If game found, return its partial state:
            else {
                PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();
                try {
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionRow(), referencedSession.getPositionCol(), referencedGame.getFullBoardState());
                    Response response = new SuccessResponse("Partial state retrieved", "Partial state retrieved.");
                    Gson gson = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    response.setData(data);
                    return response;
                }

                //If failed to get the partial state, return error:
                catch (InvalidCellReferenceException e) {
                    return new ErrorResponse("Error fetching partial state for session '" + sessionID + "'.", e.getMessage());
                }
            }
        }
    }

    @Override
    public Response move(String sessionID, int row, int col) {

        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            return new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
        }

        //If session valid, try to get the referenced game:
        else {

            Game referencedGame = Datastore.getGame(referencedSession.getGameToken());

            //If game not found for this session, return error:
            if (referencedGame == null) {
                return new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
            }

            //Check if game is started:
            if (referencedGame.getGameState().isEnded()) {
                return new ErrorResponse("Game ended", "The game with token '" + referencedGame.getGameSpecification().getToken() + "' has ended.");
            }

//            //Check for valid points:
//            if (row + referencedSession.getPartialStatePreference().getWidth() > referencedGame.getGameSpecification().getWidth() || row < 0
//            || col + referencedSession.getPartialStatePreference().getHeight() > referencedGame.getGameSpecification().getHeight() || col < 0) {
//                return new ErrorResponse("Invalid move", "The move shift to cell (" + row + ", " + col + ") is out of bounds.");
//            }

            //Extract board state:
            PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();
            PartialBoardState partialBoardState;
            try {
                partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), row, col, referencedGame.getFullBoardState());
            } catch (InvalidCellReferenceException e) {
                return new ErrorResponse("Invalid move", "The move shift to cell (" + row + ", " + col + ") is out of bounds.");
            }

            //Set the position of the session:
            referencedSession.setPositionCol(col);
            referencedSession.setPositionRow(row);

            //Reply:
            Gson gson = new Gson();
            JsonObject data = new JsonObject();
            data.add("partialBoardState", gson.toJsonTree(partialBoardState));
            data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
            return new SuccessResponse("Moved successfully", "The move shift to cell (" + row + ", " + col + ") was successful.", data);

        }

    }

    @Override
    public Response reveal(String sessionID, int row, int col) {
        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            return new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
        }

        //If session valid, try to get the referenced game:
        else {

            final Game referencedGame = Datastore.getGame(referencedSession.getGameToken());
            final PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();

            //If game not found for this session, return error:
            if (referencedGame == null) {
                return new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
            }

            //If session is spectator, return error:
            if (referencedSession.isSpectator()) {
                return new ErrorResponse("Spectator-only session", "The session with ID '" + sessionID + "' can only specatate the game.");
            }

            FullBoardState state = referencedGame.getFullBoardState();

            //Check the coordinates for validity:
            if (row >= state.getHeight() || col >= state.getWidth() || row < 0 || col < 0) {
                return new ErrorResponse("Invalid coordinates", "The coordinates (" + row + "," + col + ") are out of bounds.");
            }

            //Check if the game has started:
            if (referencedGame.getGameState() == GameState.NOT_STARTED) {
                return new ErrorResponse("Game not started", "The game you tried to play has not yet started.");
            }

            //Check if the cell is revealed:
            if (referencedGame.getFullBoardState().getCells()[row][col].getRevealState() != RevealState.COVERED) {
                try {
                    SuccessResponse response = new SuccessResponse("Cell already revealed", "The cell (" + row + "," + col + ") has already been revealed.");
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionRow(), referencedSession.getPositionCol(), referencedGame.getFullBoardState());
                    Gson gson = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    data.add("revealState", gson.toJsonTree(referencedGame.getFullBoardState().getCells()[row][col].getRevealState()));
                    data.addProperty("points", referencedSession.getPoints());
                    response.setData(data);
                    Server.updateClients(referencedSession.getGameToken(), sessionID);
                    return response;
                } catch (InvalidCellReferenceException e) {
                    return new ErrorResponse("Failed to fetch partial state", "The cell (" + row + "," + col + ") has been already revealed, but failed to load partial state: " + e.getMessage());
                }
            }

            //Reveal, change points and return partial state:
            synchronized (new Object()) {
                RevealState revealState = referencedGame.reveal(row, col);
                switch (revealState) {
                    case REVEALED_0:
                    case REVEALED_1:
                    case REVEALED_2:
                    case REVEALED_3:
                    case REVEALED_4:
                    case REVEALED_5:
                    case REVEALED_6:
                    case REVEALED_7:
                    case REVEALED_8:
                        referencedSession.changePoints(10);
                        break;
                    case REVEALED_MINE:
                        referencedSession.changePoints(-5);
                        break;
                }
            }



            //If the game has ended (player won or lost), reveal all of the cells:
            if (referencedGame.getGameState() == GameState.ENDED_LOST || referencedGame.getGameState() == GameState.ENDED_WON) {
                referencedGame.revealAll();
            }

            try {
                PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionRow(), referencedSession.getPositionCol(), referencedGame.getFullBoardState());
                Gson gson  = new Gson();
                JsonObject data = new JsonObject();
                data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                data.addProperty("points", referencedSession.getPoints());
                SuccessResponse response = new SuccessResponse("Cell revealed", "Cell (" + row + "," + col + ") revealed successfully.");
                response.setData(data);
                Server.updateClients(referencedSession.getGameToken(), sessionID);
                return response;
            } catch (InvalidCellReferenceException e) {
                return new ErrorResponse("Cell revealed, failed to fetch partial state", "The cell (" + row + "," + col + ") has been revealed, but failed to load partial state: " + e.getMessage());
            }
        }
    }

    @Override
    public Response flag(String sessionID, int row, int col) {
        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            return new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
        }

        //If session is spectator, return error:
        if (referencedSession.isSpectator()) {
            return new ErrorResponse("Spectator-only session", "The session with ID '" + sessionID + "' can only specatate the game.");
        }

        //Find the game of this session:
        final Game referencedGame = Datastore.getGame(referencedSession.getGameToken());
        final PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();

        //If game not found for this session, return error:
        if (referencedGame == null) {
            return new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
        }

        //If game found, attempt a flag action:
        else {
            FullBoardState state = referencedGame.getFullBoardState();

            //Check the coordinates for validity:
            if (row >= state.getHeight() || col >= state.getWidth() || row < 0 || col < 0) {
                return new ErrorResponse("Invalid coordinates", "The coordinates (" + row + "," + col + ") are out of bounds.");
            }

            //Check if the game has started:
            if (referencedGame.getGameState() == GameState.NOT_STARTED) {
                return new ErrorResponse("Game not started", "The game you tried to play has not yet started.");
            }

            //Check if the cell is already flagged and unflag it:
            if (referencedGame.getFullBoardState().getCells()[row][col].getRevealState() == RevealState.FLAGGED) {
                try {
                    referencedGame.flag(row, col);
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionRow(), referencedSession.getPositionCol(), referencedGame.getFullBoardState());
                    SuccessResponse successResponse = new SuccessResponse("Cell unflagged", "The cell (" + row + "," + col + ") unflagged successfully.");
                    Gson gson = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    successResponse.setData(data);
                    Server.updateClients(referencedSession.getGameToken(), sessionID);
                    return successResponse;
                } catch (InvalidCellReferenceException e) {
                    return new ErrorResponse("Cell unflagged, failed to fetch partial state", "The cell (" + row + "," + col + ") has been unflagged, but failed to load partial state: " + e.getMessage());
                }
            }

            //Check if the cell is revealed:
            if (referencedGame.getFullBoardState().getCells()[row][col].getRevealState() != RevealState.COVERED) {
                ErrorResponse response = new ErrorResponse("Cell already revealed", "The cell (" + row + "," + col + ") has already been revealed.");
                Gson gson = new Gson();
                JsonObject data = new JsonObject();
                data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                data.add("revealState", gson.toJsonTree(referencedGame.getFullBoardState().getCells()[row][col].getRevealState()));
                Server.updateClients(referencedSession.getGameToken(), sessionID);
                response.setData(data);
                return response;
            }

            //Flag and return partial state:
            synchronized (new Object()) {
                referencedGame.flag(row, col);
            }

            try {
                PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionRow(), referencedSession.getPositionCol(), referencedGame.getFullBoardState());
                Gson gson  = new Gson();
                JsonObject data = new JsonObject();
                data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                SuccessResponse response = new SuccessResponse("Cell flagged", "Cell (" + row + "," + col + ") flagged successfully.");
                response.setData(data);
                Server.updateClients(referencedSession.getGameToken(), sessionID);
                return response;
            } catch (InvalidCellReferenceException e) {
                return new ErrorResponse("Cell flagged, failed to fetch partial state", "The cell (" + row + "," + col + ") has been flagged, but failed to load partial state: " + e.getMessage());
            }

        }

    }

}
