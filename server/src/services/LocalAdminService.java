package services;

import api.AdminService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datastore.Datastore;
import exception.InvalidCellReferenceException;
import model.*;
import response.AuthErrorResponse;
import response.ErrorResponse;
import response.Response;
import response.SuccessResponse;

public class LocalAdminService implements AdminService {

    @Override
    public String createGame(String password, int maxNumOfPlayers, int width, int height, Difficulty difficulty) {

        //Authentication check:
        if (!Datastore.checkPassword(password)) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            return errorResponse.toJSON();
        }

        //Check parameters:
        if (maxNumOfPlayers < 1) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid maxNumOfPlayers", "The maximum number of players must be 1 or more.");
            return errorResponse.toJSON();
        }

        if (width < 5) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid width", "The width of a game must be 5 cells or more.");
            return errorResponse.toJSON();
        }

        if (height < 5) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid height", "The height of a game must be 5 cells or more.");
            return errorResponse.toJSON();
        }

        //Create the game:
        String gameToken = Datastore.addGame(maxNumOfPlayers, width, height, difficulty);

        if (gameToken == null) {
            ErrorResponse errorResponse = new ErrorResponse("Game creation failed", "Failed to create game (unknown).");
            return errorResponse.toJSON();
        }

        SuccessResponse successResponse = new SuccessResponse("Game created", "A game with the specified configuration was successfully created.");
        JsonObject data = new JsonObject();
        data.addProperty("gameToken", gameToken);
        successResponse.setData(data);
        return successResponse.toJSON();
    }

    @Override
    public String startGame(String password, String gameToken) {

        //Authentication check:
        if (!Datastore.checkPassword(password)) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            return errorResponse.toJSON();
        }

        //Find the game:
        Game referencedGame = Datastore.getGame(gameToken);
        if (referencedGame == null) {
            ErrorResponse response = new ErrorResponse("Game not found", "Game with token '" + gameToken + "' not found.");
            return response.toJSON();
        }

        //Start the game:
        if (referencedGame.start()) {
            SuccessResponse successResponse = new SuccessResponse("Game started", "Game with token '" + gameToken + "' started.");
            Gson gson = new Gson();
            JsonObject data = new JsonObject();
            data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
            successResponse.setData(data);
            return successResponse.toJSON();
        }
        else {
            ErrorResponse errorResponse = new ErrorResponse("Game already started or ended", "Game with token '" + gameToken + "' has already started or has ended.");
            Gson gson = new Gson();
            JsonObject data = new JsonObject();
            data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
            errorResponse.setData(data);
            return errorResponse.toJSON();
        }

    }

    @Override
    public String viewGame(String password, String gameToken, int partialStateWidth, int partialStateHeight, int startX, int startY) {
        //Authentication check:
        if (!Datastore.checkPassword(password)) {
            AuthErrorResponse errorResponse = new AuthErrorResponse();
            return errorResponse.toJSON();
        }

        //Find the game:
        Game referencedGame = Datastore.getGame(gameToken);
        if (referencedGame == null) {
            ErrorResponse response = new ErrorResponse("Game not found", "Game with token '" + gameToken + "' not found.");
            return response.toJSON();
        }

        PartialStatePreference partialStatePreference = new PartialStatePreference(partialStateWidth, partialStateHeight);

        try {
            PartialBoardState partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), startX, startY, referencedGame.getFullBoardState());
            Response response = new SuccessResponse("Game state retrieved", "Game state retrieved.");
            Gson gson = new Gson();
            JsonObject data = new JsonObject();
            data.add("partialBoardState", gson.toJsonTree(partialBoardState));
            data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
            response.setData(data);
            return response.toJSON();
        }

        //If failed to get the partial state, return error:
        catch (InvalidCellReferenceException e) {
            Response response = new ErrorResponse("Game state not retrieved", e.getMessage());
            return response.toJSON();
        }

    }

//    public String subscribe(String password, String token, PartialStatePreference partialStatePreference, int startX, int startY, ObserverForm observerForm) {
//
//        //Authentication check:
//        if (!Datastore.checkPassword(password)) {
//            AuthErrorResponse errorResponse = new AuthErrorResponse();
//            return errorResponse.toJSON();
//        }
//
//        //Check the game token:
//        Game referencedGame = Datastore.getGame(token);
//
//        if (referencedGame == null) {
//            ErrorResponse errorResponse = new ErrorResponse("Game does not exist", "The game with token '" + token + "' does not exist.");
//            return errorResponse.toJSON();
//        }
//
//        //Create a new session:
//        String sessionID = Datastore.addSession(token, "admin", partialStatePreference);
//        SuccessResponse successResponse = new SuccessResponse("Game joined", "Successfully joined game with ID '" + token + "'.");
//        JsonObject data = new JsonObject();
//        data.addProperty("sessionID", sessionID);
//        data.addProperty("totalWidth", referencedGame.getGameSpecification().getWidth());
//        data.addProperty("totalHeight", referencedGame.getGameSpecification().getHeight());
//        successResponse.setData(data);
//        if (observerForm != null) referencedGame.addObserver(sessionID, observerForm);
//        return successResponse.toJSON();
//
//    }

}
