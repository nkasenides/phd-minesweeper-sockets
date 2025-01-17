package services;

import api.MasterService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import datastore.Datastore;
import exception.InvalidCellReferenceException;
import model.*;
import response.ErrorResponse;
import response.JsonConvert;
import response.Response;
import response.SuccessResponse;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class LocalMasterService implements MasterService {

    @Override
    public Response listGames() {
        ArrayList<String> games = Datastore.getGames();

        //Check if games exist:
        if (games.size() < 1) {
            return new SuccessResponse("No games found", "No games have been found.");
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
            return successResponse;
        }
    }

    @Override
    public Response join(String token, String playerName, int partialStateWidth, int partialStateHeight) {

        //Check the game token:
        Game referencedGame = Datastore.getGame(token);

        if (referencedGame == null) {
            return new ErrorResponse("Game does not exist", "The game with token '" + token + "' does not exist.");
        }

        //Filter player name:
        playerName = playerName.toLowerCase().trim();
        Pattern p = Pattern.compile("^[a-zA-Z0-9]*$");
        if (!p.matcher(playerName).find()) {
            return new ErrorResponse("Invalid player name", "The player name must contain alphanumeric characters only.");
        }

        //Check if the player's name exists in this game:
        for (String sessionID : Datastore.getSessions()) {
            Session s = Datastore.getSession(sessionID);
            if (s.getGameToken().equals(token)) {
                if (s.getPlayerName().toLowerCase().equals(playerName)) {
                    return new ErrorResponse("Player already exists", "The player with name '" + playerName + "' already exists in game with ID '" + token + "'");
                }
            }
        }

        //Check for max num of players:
        int sessionsInThisGame = 0;
        for (String sID : Datastore.getSessions()) {
            Session s = Datastore.getSession(sID);
            if (!s.isSpectator()) {
                if (s.getGameToken().equals(token)) {
                    sessionsInThisGame++;
                }
            }
        }
        if (sessionsInThisGame >= referencedGame.getGameSpecification().getMaxPlayers()) {
            return new ErrorResponse("Max players reached", "The game with token '" + token + "' is full (" + sessionsInThisGame + "/" + referencedGame.getGameSpecification().getMaxPlayers() + ").");
        }

        PartialStatePreference partialStatePreference = new PartialStatePreference(partialStateWidth, partialStateHeight);

        //Create a new session:
        String sessionID = Datastore.addSession(token, playerName, partialStatePreference, false);
        Session session = Datastore.getSession(sessionID);
        PartialBoardState partialBoardState;

        try {
            partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), session.getPositionCol(), session.getPositionRow(), referencedGame.getFullBoardState());
        }
        //If failed to get the partial state, return error:
        catch (InvalidCellReferenceException e) {
            return new ErrorResponse("Error fetching partial state for session '" + sessionID + "'.", e.getMessage());
        }

        SuccessResponse successResponse = new SuccessResponse("Game joined", "Successfully joined game with ID '" + token + "'.");
        JsonObject data = new JsonObject();
        Gson gson = new Gson();
        data.addProperty("sessionID", sessionID);
        data.addProperty("totalWidth", referencedGame.getGameSpecification().getWidth());
        data.addProperty("totalHeight", referencedGame.getGameSpecification().getHeight());
        data.add("partialBoardState", gson.toJsonTree(partialBoardState));
        data.add("gameState", gson.toJsonTree(referencedGame.getGameState()));
        successResponse.setData(data);
        return successResponse;
    }

}
