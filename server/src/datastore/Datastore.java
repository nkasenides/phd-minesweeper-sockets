package datastore;

import model.*;

import java.util.*;

public class Datastore {

    private static Map<String, Game> games = Collections.synchronizedMap(new HashMap<>());  //  Game token -> Game object
    private static Map<String, Session> sessions = Collections.synchronizedMap(new HashMap<>());  // Session ID -> Session object
    private static String adminPassword = "1234"; //Administrator's password, gives access to AdminService.

    public static Session getSession(String sessionID) {
        return sessions.get(sessionID);
    }

    public static Game getGame(String gameID) {
        return games.get(gameID);
    }

    public static ArrayList<String> getGames() {
        return new ArrayList<>(games.keySet());
    }

    public static ArrayList<String> getSessions() {
        return new ArrayList<>(sessions.keySet());
    }

    public static String addGame(int maxPlayers, int width, int height, Difficulty difficulty) {
        String gameID = UUID.randomUUID().toString();
        GameSpecification gameSpecification = new GameSpecification(gameID, maxPlayers, width, height, difficulty);
        Game game = new Game(gameSpecification);
        games.put(gameID, game);
        return gameID;
    }

    public static String addSession(String gameID, String playerName, PartialStatePreference partialStatePreference, boolean spectator) {
        Game referencedGame = games.get(gameID);
        if (referencedGame == null) return null;
        Session session = new Session(partialStatePreference, playerName, gameID, spectator);
        sessions.put(session.getSessionID(), session);
        return session.getSessionID();
    }

    public static boolean removeSession(String sessionID) {
        return sessions.remove(sessionID) != null;
    }

    public static boolean removeGame(String gameID) {
        return games.remove(gameID) != null;
    }

    public static boolean checkPassword(String password) {
        return (password.equals(adminPassword));
    }

}
