package services;

import api.MasterService;
import com.google.gson.JsonObject;
import datastore.Datastore;
import model.*;
import respondx.ErrorResponse;
import respondx.JsonConvert;
import respondx.SuccessResponse;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LocalMasterService implements MasterService {

    @Override
    public String listGames() {
        ArrayList<String> games = Datastore.getGames();

        //Check if games exist:
        if (games.size() < 1) {
            SuccessResponse successResponse = new SuccessResponse("No games found", "No games have been found.");
            return successResponse.toJSON();
        }
        else {
            SuccessResponse successResponse = new SuccessResponse("Games retrieved", games.size() + " games retrieved.");
            JsonObject data = new JsonObject();

            ArrayList<GameSpecification> gameInstances = new ArrayList<>();
            for (String token : games) {
                Game game = Datastore.getGame(token);
                gameInstances.add(game.getGameSpecification());
            }

            data.add("games", JsonConvert.listToJsonArray(gameInstances));
            successResponse.setData(data);
            return successResponse.toJSON();
        }
    }

    @Override
    public String join(String token, String playerName, PartialStatePreference partialStatePreference) {

        //Check the game token:
        Game referencedGame = Datastore.getGame(token);

        if (referencedGame == null) {
            ErrorResponse errorResponse = new ErrorResponse("Game does not exist", "The game with token '" + token + "' does not exist.");
            return errorResponse.toJSON();
        }

        //Filter player name:
        playerName = playerName.toLowerCase().trim();
        Pattern p = Pattern.compile("^[a-zA-Z0-9]*$");
        if (!p.matcher(playerName).find()) {
            ErrorResponse errorResponse = new ErrorResponse("Invalid player name", "The player name must contain alphanumeric characters only.");
            return errorResponse.toJSON();
        }

        //Check if the player's name exists in this game:
        for (String sessionID : Datastore.getSessions()) {
            Session s = Datastore.getSession(sessionID);
            if (s.getGameID().equals(token)) {
                if (s.getPlayerName().toLowerCase().equals(playerName)) {
                    ErrorResponse errorResponse = new ErrorResponse("Player already exists", "The player with name '" + playerName + "' already exists in game with ID '" + token + "'");
                    return errorResponse.toJSON();
                }
            }
        }

        //Create a new session:
        String sessionID = Datastore.addSession(token, playerName, partialStatePreference);
        SuccessResponse successResponse = new SuccessResponse("Game joined", "Successfully joined game with ID '" + token + "'.");
        JsonObject data = new JsonObject();
        data.addProperty("sessionID", sessionID);
        data.addProperty("totalWidth", referencedGame.getGameSpecification().getWidth());
        data.addProperty("totalHeight", referencedGame.getGameSpecification().getHeight());
        successResponse.setData(data);
        return successResponse.toJSON();
    }

//    public String joinLocal(String token, String playerName, PartialStatePreference partialStatePreference, ObserverForm observerForm) {
//
//        //Check the game token:
//        Game referencedGame = Datastore.getGame(token);
//
//        if (referencedGame == null) {
//            ErrorResponse errorResponse = new ErrorResponse("Game does not exist", "The game with token '" + token + "' does not exist.");
//            return errorResponse.toJSON();
//        }
//
//        //Filter player name:
//        playerName = playerName.toLowerCase().trim();
//        Pattern p = Pattern.compile("^[a-zA-Z0-9]*$");
//        if (!p.matcher(playerName).find()) {
//            ErrorResponse errorResponse = new ErrorResponse("Invalid player name", "The player name must contain alphanumeric characters only.");
//            return errorResponse.toJSON();
//        }
//
//        //Check if the player's name exists in this game:
//        for (String sessionID : Datastore.getSessions()) {
//            Session s = Datastore.getSession(sessionID);
//            if (s.getGameID().equals(token)) {
//                if (s.getPlayerName().toLowerCase().equals(playerName)) {
//                    ErrorResponse errorResponse = new ErrorResponse("Player already exists", "The player with name '" + playerName + "' already exists in game with ID '" + token + "'");
//                    return errorResponse.toJSON();
//                }
//            }
//        }
//
//        //Create a new session:
//        String sessionID = Datastore.addSession(token, playerName, partialStatePreference);
//        SuccessResponse successResponse = new SuccessResponse("Game joined", "Successfully joined game with ID '" + token + "'.");
//        JsonObject data = new JsonObject();
//        data.addProperty("sessionID", sessionID);
//        data.addProperty("totalWidth", referencedGame.getGameSpecification().getWidth());
//        data.addProperty("totalHeight", referencedGame.getGameSpecification().getHeight());
//        successResponse.setData(data);
//        if (observerForm != null) referencedGame.addObserver(sessionID, observerForm);
//        return successResponse.toJSON();
//    }

}
