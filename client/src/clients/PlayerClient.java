package clients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.*;
import response.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import static response.ResponseStatus.OK;

public class PlayerClient implements Runnable {

    public static final int SERVER_PORT = 12345;
    private ArrayList<GameSpecification> games = null;
    private GameSpecification gameSpecification = null;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String name;
    private final int turnInterval;
    private final PartialStatePreference partialStatePreference;

    private String sessionID = null;
    private int gameWidth;
    private int gameHeight;

    private int xShift = 0;
    private int yShift = 0;
    private GameState gameState;
    private PartialBoardState partialBoardState;

    public PlayerClient(Socket socket, String name, int turnInterval, PartialStatePreference partialStatePreference) throws IOException {
        this.name = name;
        this.turnInterval = turnInterval;
        this.partialStatePreference = partialStatePreference;
        printWriter = new PrintWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {

            System.out.println(name + ": Acquiring a list of games...");

            //List the games:
            Command listGamesCommand = new Command(CommandType.MASTER_SERVICE, "listGames", null);
            Gson gson = new Gson();
            String commandJSON = gson.toJson(listGamesCommand);
            printWriter.println(commandJSON);
            printWriter.flush();
            String listReply = bufferedReader.readLine();
            Response listResponse = gson.fromJson(listReply, Response.class);
            if (listResponse.getStatus() == OK) {
                JsonArray gamesArray = listResponse.getData().get("games").getAsJsonArray();
                games = new ArrayList<>();
                for (JsonElement e : gamesArray) {
                    GameSpecification s = gson.fromJson(e, GameSpecification.class);
                    games.add(s);
                }
            }

            System.out.println(name + ": Games fetched.");

            if (games.size() < 1) {
                throw new RuntimeException("Error - No games found");
            }

            System.out.println(name + ": Joining game with token '" + games.get(0).getToken() + "'...");

            //Try to join the game:
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", games.get(0).getToken());
            jsonObject.addProperty("playerName", name);
            jsonObject.addProperty("partialStateWidth", partialStatePreference.getWidth());
            jsonObject.addProperty("partialStateHeight", partialStatePreference.getHeight());
            Command joinGameCommand = new Command(CommandType.MASTER_SERVICE, "join", jsonObject);
            String joinCommandJSON = gson.toJson(joinGameCommand);
            printWriter.println(joinCommandJSON);
            printWriter.flush();
            String joinReply = bufferedReader.readLine();
            Response joinResponse = gson.fromJson(joinReply, Response.class);
            if (joinResponse.getStatus() == OK) {
                gameSpecification = games.get(0);
                sessionID = joinResponse.getData().get("sessionID").getAsString();
                gameWidth = joinResponse.getData().get("totalWidth").getAsInt();
                gameHeight = joinResponse.getData().get("totalHeight").getAsInt();
                System.out.println(name + ": Joined game with token '" + games.get(0).getToken() + " with session ID '" + sessionID + "'.");
            }

            System.out.println(name + ": Starting to play!");

            //While the game is not over, keep making moves:
            while (true) {

                /**
                 * TODO 1) Make the players check the partial board state version of the cell they try to play on. If revealed, choose another cell.
                 * TODO 2) Use the partial board state to play
                 * TODO 3) Make the players to shift position automatically when there are no unrevealed cells in board state.
                 */

                JsonObject object = new JsonObject();
                Random random = new Random();
                int randomX = random.nextInt(games.get(0).getWidth()); //TODO Algorithm would ideally only try to solve cells on the partial state...
                int randomY = random.nextInt(games.get(0).getHeight()); //TODO Algorithm would ideally only try to solve cells on the partial state...
                String moveEndpoint = random.nextInt(10) > 6 ? "flag" : "reveal";

                System.out.println(name + ": Decided to make move '" + moveEndpoint + "' at cell (" + randomX + "," + randomY + ").");

                object.addProperty("x", randomX);
                object.addProperty("y", randomY);
                object.addProperty("sessionID", sessionID);
                Command command = new Command(CommandType.USER_SERVICE, moveEndpoint, object);

                //Convert to JSON and send:
                String moveCommandJSON = gson.toJson(command);
                printWriter.println(moveCommandJSON);
                printWriter.flush();

                //Wait for and print reply:
                String reply = bufferedReader.readLine();
                System.out.println("[" + name + "] Got: '" + reply + "'");

                Response playResponse = gson.fromJson(reply, Response.class);
                if (playResponse.getStatus() == OK) {
                    System.out.println(name + ": Made move '" + moveEndpoint + "' at cell (" + randomX + "," + randomY + ").");
                    JsonElement gameStateElement = playResponse.getData().get("gameState");
                    GameState gameState = gson.fromJson(gameStateElement, GameState.class);
                    if (gameState.isEnded()) {
                        System.out.println(name + ": GAME ENDED (" + gameState + ")");
                        return;
                    }
                }

                Thread.sleep(turnInterval);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public static void main(String[] args) {

        final String USAGE = "Use: PlayerClient <SERVER_IP_ADDRESS> <NUM_OF_CLIENTS> <PARTIAL_STATE_WIDTH> <PARTIAL_STATE_HEIGHT> <TURN_INTERVAL>";

        if (args.length < 5) {
            System.out.println(USAGE);
        }

        String ipAddress = args[0];
        int numOfClients;
        int partialStateWidth;
        int partialStateHeight;
        int turnInterval;
        try {
            numOfClients = Integer.parseInt(args[1]);
            partialStateWidth = Integer.parseInt(args[2]);
            partialStateHeight = Integer.parseInt(args[3]);
            turnInterval = Integer.parseInt(args[4]);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (numOfClients < 1) {
            System.out.println("Invalid number of clients. The value must be more than or equal to 1.");
            return;
        }

        if (partialStateWidth < 5) {
            System.out.println("Invalid partial state width. The value must be more than or equal to 5.");
            return;
        }

        if (partialStateHeight < 5) {
            System.out.println("Invalid partial state height. The value must be more than or equal to 5.");
            return;
        }

        if (turnInterval < 0) {
            System.out.println("Invalid turn interval. The value must be more than or equal to 0 and provided in milliseconds.");
        }

        PlayerClient[] clients = new PlayerClient[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket(ipAddress, SERVER_PORT);
                clients[i] = new PlayerClient(socket, "player" + (i + 1), turnInterval, new PartialStatePreference(partialStateWidth, partialStateHeight));
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}

