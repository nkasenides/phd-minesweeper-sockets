package services;

import api.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datastore.Datastore;
import exception.InvalidCellReferenceException;
import model.*;
import respondx.ErrorResponse;
import respondx.Response;
import respondx.SuccessResponse;

public class LocalUserService implements UserService {

    @Override
    public String getPartialState(String sessionID) {

        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            Response response = new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
            return response.toJSON();
        }

        //If session valid, try to get the referenced game:
        else {

            Game referencedGame = Datastore.getGame(referencedSession.getGameID());

            //If game not found for this session, return error:
            if (referencedGame == null) {
                Response response = new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
                return response.toJSON();
            }

            //If game found, return its partial state:
            else {
                PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();
                try {
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                    Response response = new SuccessResponse("Partial state retrieved", "Partial state retrieved.");
                    Gson gson = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    response.setData(data);
                    return response.toJSON();
                }

                //If failed to get the partial state, return error:
                catch (InvalidCellReferenceException e) {
                    Response response = new ErrorResponse("Error fetching partial state for session '" + sessionID + "'.", e.getMessage());
                    return response.toJSON();
                }
            }
        }
    }

    @Override
    public String move(String sessionID, Direction direction, int unitOfMovement) {

        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            Response response = new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
            return response.toJSON();
        }

        //If session valid, try to get the referenced game:
        else {

            Game referencedGame = Datastore.getGame(referencedSession.getGameID());

            //If game not found for this session, return error:
            if (referencedGame == null) {
                Response response = new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
                return response.toJSON();
            }

            //If game found, attempt a shift/move action:
            else {

                //Check units:
                if (unitOfMovement < 1) {
                    ErrorResponse errorResponse = new ErrorResponse("Invalid move", "Unit of movement must be 1 or more.");
                    return errorResponse.toJSON();
                }

                final int currentX = referencedSession.getPositionX();
                final int currentY = referencedSession.getPositionY();
                boolean shifted = false;
                switch (direction) {
                    case UP:
                        if (currentX - unitOfMovement >= 0) {
                            referencedSession.setPositionX(currentX - unitOfMovement);
                            shifted = true;
                        }
                        break;
                    case DOWN:
                        if (currentX + unitOfMovement <= referencedGame.getGameSpecification().getWidth() - 1) {
                            referencedSession.setPositionX(currentX + unitOfMovement);
                            shifted = true;
                        }
                        break;
                    case LEFT:
                        if (currentY - unitOfMovement >= 0) {
                            referencedSession.setPositionY(currentY - unitOfMovement);
                            shifted = true;
                        }
                        break;
                    case RIGHT:
                        if (currentY + unitOfMovement <= referencedGame.getGameSpecification().getHeight() - 1) {
                            referencedSession.setPositionY(currentY + unitOfMovement);
                            shifted = true;
                        }
                        break;
                }

                if (shifted) {
                    PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();
                    try {
                        PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                        Gson gson  = new Gson();
                        JsonObject data = new JsonObject();
                        data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                        data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                        SuccessResponse response = new SuccessResponse("Position shifted", "The position was shifted by " + unitOfMovement + " cells " + direction.getName() + "wards.");
                        response.setData(data);
//                        referencedGame.updateObserver(sessionID);
                        return response.toJSON();
                    } catch (InvalidCellReferenceException e) {
                        ErrorResponse errorResponse = new ErrorResponse("Position not shifted", "Could not shift position: " + e.getMessage());
                        return errorResponse.toJSON();
                    }
                }
                else {
                    ErrorResponse response = new ErrorResponse("Position not shifted", "Failed to shift position by " + unitOfMovement + " cells " + direction.getName() + "wards. The intended position is not valid.");
                    return response.toJSON();
                }

            }
        }

    }

    @Override
    public String reveal(String sessionID, int x, int y) {
        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            Response response = new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
            return response.toJSON();
        }

        //If session valid, try to get the referenced game:
        else {

            final Game referencedGame = Datastore.getGame(referencedSession.getGameID());
            final PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();

            //If game not found for this session, return error:
            if (referencedGame == null) {
                Response response = new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
                return response.toJSON();
            }

            //If game found, attempt a reveal action:
            else {
                FullBoardState state = referencedGame.getFullBoardState();

                //Check the coordinates for validity:
                if (x >= state.getWidth() || y >= state.getHeight() || x < 0 || y < 0) {
                    ErrorResponse response = new ErrorResponse("Invalid coordinates", "The coordinates (" + x + "," + y + ") are out of bounds.");
                    return response.toJSON();
                }

                //Check if the game has started:
                if (referencedGame.getGameState() == GameState.NOT_STARTED) {
                    ErrorResponse response = new ErrorResponse("Game not started", "The game you tried to play has not yet started.");
                    return response.toJSON();
                }

                //Check if the cell is revealed:
                if (referencedGame.getFullBoardState().getCells()[x][y].getRevealState() != RevealState.COVERED) {
                    try {
                        SuccessResponse response = new SuccessResponse("Cell already revealed", "The cell (" + x + "," + y + ") has already been revealed.");
                        PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                        Gson gson = new Gson();
                        JsonObject data = new JsonObject();
                        data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                        data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                        data.add("revealState", gson.toJsonTree(referencedGame.getFullBoardState().getCells()[x][y].getRevealState()));
                        response.setData(data);
//                        referencedGame.updateObservers();
                        return response.toJSON();
                    } catch (InvalidCellReferenceException e) {
                        ErrorResponse errorResponse = new ErrorResponse("Failed to fetch partial state", "The cell (" + x + "," + y + ") has been already revealed, but failed to load partial state: " + e.getMessage());
                        return errorResponse.toJSON();
                    }

                }

                //Reveal and return partial state:
                referencedGame.reveal(x, y);

                //If the game has ended (player won or lost), reveal all of the cells:
                if (referencedGame.getGameState() == GameState.ENDED_LOST ||
                    referencedGame.getGameState() == GameState.ENDED_WON) {
                    referencedGame.revealAll();
                }

                try {
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                    Gson gson  = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    SuccessResponse response = new SuccessResponse("Cell revealed", "Cell (" + x + "," + y + ") revealed successfully.");
                    response.setData(data);
//                    referencedGame.updateObservers();
                    return response.toJSON();
                } catch (InvalidCellReferenceException e) {
                    ErrorResponse errorResponse = new ErrorResponse("Cell revealed, failed to fetch partial state", "The cell (" + x + "," + y + ") has been revealed, but failed to load partial state: " + e.getMessage());
                    return errorResponse.toJSON();
                }

            }
        }
    }

    @Override
    public String flag(String sessionID, int x, int y) {
        //Retrieve referenced session and check if valid:
        Session referencedSession = Datastore.getSession(sessionID);

        //If session not valid, return error:
        if (referencedSession == null) {
            Response response = new ErrorResponse("Session not found", "Could not find session with ID '" + sessionID + "'");
            return response.toJSON();
        }

        //If session valid, try to get the referenced game:
        else {

            final Game referencedGame = Datastore.getGame(referencedSession.getGameID());
            final PartialStatePreference partialStatePreference = referencedSession.getPartialStatePreference();

            //If game not found for this session, return error:
            if (referencedGame == null) {
                Response response = new ErrorResponse("Game not found", "Could not find a game for the session with ID '" + sessionID + "'");
                return response.toJSON();
            }

            //If game found, attempt a flag action:
            else {
                FullBoardState state = referencedGame.getFullBoardState();

                //Check the coordinates for validity:
                if (x >= state.getWidth() || y >= state.getHeight() || x < 0 || y < 0) {
                    ErrorResponse response = new ErrorResponse("Invalid coordinates", "The coordinates (" + x + "," + y + ") are out of bounds.");
                    return response.toJSON();
                }

                //Check if the game has started:
                if (referencedGame.getGameState() == GameState.NOT_STARTED) {
                    ErrorResponse response = new ErrorResponse("Game not started", "The game you tried to play has not yet started.");
                    return response.toJSON();
                }

                //Check if the cell is already flagged and unflag it:
                if (referencedGame.getFullBoardState().getCells()[x][y].getRevealState() == RevealState.FLAGGED) {
                    try {
                        referencedGame.flag(x, y);
                        PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                        SuccessResponse successResponse = new SuccessResponse("Cell unflagged", "The cell (" + x + "," + y + ") unflagged successfully.");
                        Gson gson = new Gson();
                        JsonObject data = new JsonObject();
                        data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                        data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                        successResponse.setData(data);
//                        referencedGame.updateObservers();
                        return successResponse.toJSON();
                    } catch (InvalidCellReferenceException e) {
                        ErrorResponse errorResponse = new ErrorResponse("Cell unflagged, failed to fetch partial state", "The cell (" + x + "," + y + ") has been unflagged, but failed to load partial state: " + e.getMessage());
                        return errorResponse.toJSON();
                    }
                }

                //Check if the cell is revealed:
                if (referencedGame.getFullBoardState().getCells()[x][y].getRevealState() != RevealState.COVERED) {
                    ErrorResponse response = new ErrorResponse("Cell already revealed", "The cell (" + x + "," + y + ") has already been revealed.");
                    Gson gson = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    data.add("revealState", gson.toJsonTree(referencedGame.getFullBoardState().getCells()[x][y].getRevealState()));
//                    referencedGame.updateObservers();
                    response.setData(data);
                    return response.toJSON();
                }

                //Flag and return partial state:
                referencedGame.flag(x, y);

                try {
                    PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), referencedSession.getPositionX(), referencedSession.getPositionY(), referencedGame.getFullBoardState());
                    Gson gson  = new Gson();
                    JsonObject data = new JsonObject();
                    data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
                    data.add("partialBoardState", gson.toJsonTree(partialBoardState));
                    SuccessResponse response = new SuccessResponse("Cell flagged", "Cell (" + x + "," + y + ") flagged successfully.");
                    response.setData(data);
//                    referencedGame.updateObservers();
                    return response.toJSON();
                } catch (InvalidCellReferenceException e) {
                    ErrorResponse errorResponse = new ErrorResponse("Cell flagged, failed to fetch partial state", "The cell (" + x + "," + y + ") has been flagged, but failed to load partial state: " + e.getMessage());
                    return errorResponse.toJSON();
                }

            }
        }
    }

}
