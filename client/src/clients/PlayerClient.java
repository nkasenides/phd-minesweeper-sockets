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
    public static final long DELAY = 1000L;
    private ArrayList<GameSpecification> games = null;
    private GameSpecification gameSpecification = null;
    private String sessionID = null;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String name;

    public PlayerClient(Socket socket, String name) throws IOException {
        this.name = name;
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
//            System.out.println("Sending request --> " + commandJSON);
            printWriter.flush();
            String listReply = bufferedReader.readLine();
//            System.out.println("Got: '" + listReply + "'");
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
            jsonObject.addProperty("partialStateWidth", 10);
            jsonObject.addProperty("partialStateHeight", 10);
            Command joinGameCommand = new Command(CommandType.MASTER_SERVICE, "join", jsonObject);
            String joinCommandJSON = gson.toJson(joinGameCommand);
            printWriter.println(joinCommandJSON);
//            System.out.println("Sending request --> " + joinCommandJSON);
            printWriter.flush();
            String joinReply = bufferedReader.readLine();
//            System.out.println("Got: '" + joinReply + "'");
            Response joinResponse = gson.fromJson(joinReply, Response.class);
            if (joinResponse.getStatus() == OK) {
                gameSpecification = games.get(0);
                sessionID = joinResponse.getData().get("sessionID").getAsString();
                System.out.println(name + ": Joined game with token '" + games.get(0).getToken() + " with session ID '" + sessionID + "'.");
            }

            System.out.println("Starting to play!");

            //While the game is not over, keep making moves:
            while (true) {

                //TODO Generalize
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
//                System.out.println("Sending request --> " +moveCommandJSON);
                printWriter.flush();

                Thread.sleep(500); //TODO Generalize

                //Wait for and print reply:
//                System.out.println("Waiting for reply...");
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

                Thread.sleep(DELAY);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public static void main(String[] args) {
        int numOfClients = 1;
        PlayerClient[] clients = new PlayerClient[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket("localhost", SERVER_PORT);
                clients[i] = new PlayerClient(socket, "player" + (i + 1));
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}

