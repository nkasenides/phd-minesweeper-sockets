package clients;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.*;
import response.Response;
import response.ResponseStatus;
import ui.form.AdminGameForm;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static response.ResponseStatus.OK;

public class AdminClient implements Runnable {

    public static final int SERVER_PORT = 12345;
    public static final long DELAY = 1000L;
    public static final String PASSWORD = "1234";

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String name;
    private AdminGameForm gameForm;
    private PartialStatePreference partialStatePreference;
    private String sessionID;
    private String token;
    private boolean stateInitialized = false;

    public AdminClient(Socket socket, String name, PartialStatePreference partialStatePreference) throws IOException {
        this.name = name;
        this.partialStatePreference = partialStatePreference;
        printWriter = new PrintWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {

        try {

            //--Create game:
            {

                System.out.println("Creating game...");

                //Create request object:
                JsonObject object = new JsonObject();
                object.addProperty("maxNumOfPlayers", "5");
                object.addProperty("difficulty", Difficulty.HARD.toString());
                object.addProperty("width", 10); //TODO Generalize
                object.addProperty("height", 10); //TODO Generalize
                object.addProperty("password", "1234");
                Command command = new Command(CommandType.ADMIN_SERVICE, "createGame", object);

                //Convert to JSON and send:
                Gson gson = new Gson();
                String commandJSON = gson.toJson(command);
                printWriter.println(commandJSON);
                printWriter.flush();

                //Wait for and print reply:
                String reply = bufferedReader.readLine();

                Response createResponse = gson.fromJson(reply, Response.class);
                if (createResponse.getStatus() == OK) {
                    token = createResponse.getData().get("gameToken").getAsString();
                    System.out.println("Game with token '" + token + "' created.");
                }
            }

            //--Start game:
            {

                System.out.println("Starting game with token '" + token + "'.");

                //Create request object:
                JsonObject object = new JsonObject();
                object.addProperty("gameToken", token);
                object.addProperty("password", "1234");
                Command command = new Command(CommandType.ADMIN_SERVICE, "startGame", object);

                //Convert to JSON and send:
                Gson gson = new Gson();
                String commandJSON = gson.toJson(command);
                printWriter.println(commandJSON);
                printWriter.flush();

                //Wait for and print reply:
                String reply = bufferedReader.readLine();

                Response startResponse = gson.fromJson(reply, Response.class);
                if (startResponse.getStatus() == OK) {
                    System.out.println("Game with token '" + token + "' started.");
                }
            }

            //--Subscribe to the game
            {
                System.out.println("Subscribing to game with token '" + token + "'.");

                //Create request object:
                JsonObject object = new JsonObject();
                object.addProperty("token", token);
                object.addProperty("partialStateWidth", partialStatePreference.getWidth());
                object.addProperty("partialStateHeight", partialStatePreference.getHeight());
                object.addProperty("password", "1234");
                Command command = new Command(CommandType.ADMIN_SERVICE, "subscribe", object);

                //Convert to JSON and send:
                Gson gson = new Gson();
                String commandJSON = gson.toJson(command);
                printWriter.println(commandJSON);
                printWriter.flush();

                //Wait for and print reply:
                String reply = bufferedReader.readLine();

                Response response = gson.fromJson(reply, Response.class);
                if (response.getStatus() == OK) {
                    sessionID = response.getData().get("sessionID").getAsString();
                    int totalWidth = response.getData().get("totalWidth").getAsInt();
                    int totalHeight = response.getData().get("totalHeight").getAsInt();
                    gameForm = new AdminGameForm(token, totalWidth, totalHeight, partialStatePreference, this);
                    System.out.println("Subscribed to game with token '" + token + "' with session ID '" + sessionID + "'.");
                }
            }

            //Update/Initialize the game state:
            viewGame(0, 0);

            //Wait for a state-update message from the server:
            while (true) {
                String reply = bufferedReader.readLine();
                System.out.println("[" + name + "] Got: '" + reply + "'");
                Gson gson = new Gson();
                Command command = gson.fromJson(reply, Command.class);
                if (command.getCommandType() == CommandType.CLIENT_UPDATE_SERVICE) {
                    JsonObject payload = command.getPayload();
                    JsonElement gameStateElement = payload.get("gameState");
                    GameState gameState = gson.fromJson(gameStateElement, GameState.class);
                    JsonElement partialBoardStateElement = payload.get("partialBoardState");
                    PartialBoardState partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);

                    //DEBUGGING:
                    System.out.println(gson.toJson(gameState));
                    System.out.println(gson.toJson(partialBoardState));

                    gameForm.setGameState(gameState);
                    gameForm.setPartialBoardState(partialBoardState);
                    gameForm.update();
                }
            }


            //Delay:
//                Thread.sleep(DELAY);
//            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    public void viewGame(int startX, int startY) {
        System.out.println("Viewing game with token '" + token + "'.");

        //Create request object:
        JsonObject object = new JsonObject();
        object.addProperty("password", PASSWORD);
        object.addProperty("gameToken", token);
        object.addProperty("partialStateWidth", partialStatePreference.getWidth());
        object.addProperty("partialStateHeight", partialStatePreference.getHeight());
        object.addProperty("startX", startX);
        object.addProperty("startY", startY);
        Command command = new Command(CommandType.ADMIN_SERVICE, "viewGame", object);

        //Convert to JSON and send:
        Gson gson = new Gson();
        String commandJSON = gson.toJson(command);
        printWriter.println(commandJSON);
        printWriter.flush();
        String reply = null;
        try {
            reply = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response response = gson.fromJson(reply, Response.class);
        if (response.getStatus() == OK) {
            JsonObject data = response.getData();
            JsonElement gameStateElement = data.get("gameState");
            GameState gameState = gson.fromJson(gameStateElement, GameState.class);
            JsonElement partialBoardStateElement = data.get("partialBoardState");
            PartialBoardState partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
            gameForm.setGameState(gameState);
            gameForm.setPartialBoardState(partialBoardState);
            if (!stateInitialized) {
                gameForm.initialize();
                stateInitialized = true;
            }
            gameForm.update();
        }
    }

    public static void main(String[] args) {
        int numOfClients = 1;
        AdminClient[] clients = new AdminClient[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket("localhost", SERVER_PORT);
                clients[i] = new AdminClient(socket, "adminClient" + i, new PartialStatePreference(10, 10)); //TODO Generalize
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
