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
    public static final String PASSWORD = "1234";

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private final String name;
    private AdminGameForm gameForm;
    private final boolean GUI;
    private boolean stateInitialized = false;

    //Properties
    private final Difficulty difficulty;
    private String sessionID;
    private String token;
    private final PartialStatePreference partialStatePreference;
    private final int gameWidth;
    private final int gameHeight;
    private final int maxPlayers;

    //State
    public int xShift = 0;
    public int yShift = 0;
    private GameState gameState;
    private PartialBoardState partialBoardState;

    public AdminClient(Socket socket, String name, Difficulty difficulty, int gameWidth, int gameHeight, int maxPlayers,
                       PartialStatePreference partialStatePreference, boolean gui) throws IOException {
        this.name = name;
        this.partialStatePreference = partialStatePreference;
        this.difficulty = difficulty;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.maxPlayers = maxPlayers;
        this.GUI = gui;
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
                object.addProperty("maxNumOfPlayers", maxPlayers);
                object.addProperty("difficulty", difficulty.toString());
                object.addProperty("width", gameWidth);
                object.addProperty("height", gameHeight);
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
                    if (GUI) {
                        gameForm = new AdminGameForm(this);
                    }
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

                    this.gameState = gameState;
                    this.partialBoardState = partialBoardState;

                    //DEBUGGING:
                    System.out.println(gson.toJson(gameState));
                    System.out.println(gson.toJson(partialBoardState));

                    if (GUI) {
                        this.gameState = gameState;
                        this.partialBoardState = partialBoardState;
                        gameForm.update();
                    }

                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
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
            if (GUI) {
                this.gameState = gameState;
                this.partialBoardState = partialBoardState;
                if (!stateInitialized) {
                    gameForm.initialize();
                    stateInitialized = true;
                }
                gameForm.update();
            }
        }
    }

    public static void main(String[] args) {

        final String USAGE = "Use: AdminClient <SERVER_IP_ADDRESS> <GAME_WIDTH> <GAME_HEIGHT> <DIFFICULTY> <MAX_PLAYERS> <PARTIAL_STATE_WIDTH> <PARTIAL_STATE_HEIGHT> <UI>";

        if (args.length < 7) {
            System.out.println(USAGE);
        }

        String ipAddress = args[0];
        int gameWidth;
        int gameHeight;
        Difficulty difficulty;
        int maxPlayers;
        int partialStateWidth;
        int partialStateHeight;
        boolean gui = false;
        try {
            gameWidth = Integer.parseInt(args[1]);
            gameHeight = Integer.parseInt(args[2]);
            difficulty = Difficulty.valueOf(args[3]);
            maxPlayers = Integer.parseInt(args[4]);
            partialStateWidth = Integer.parseInt(args[5]);
            partialStateHeight = Integer.parseInt(args[6]);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        if (gameWidth < 5) {
            System.out.println("Invalid game width. The value must be more than or equal to 5.");
            return;
        }

        if (gameHeight < 5) {
            System.out.println("Invalid game height. The value must be more than or equal to 5.");
            return;
        }

        if (maxPlayers < 1) {
            System.out.println("Invalid max players. The value must be more than or equal to 1.");
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

        if (args.length == 8 && args[7].toLowerCase().equals("gui")) {
            gui = true;
        }

        final int numOfClients = 1;
        AdminClient[] clients = new AdminClient[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket(ipAddress, SERVER_PORT);
                clients[i] = new AdminClient(socket, "adminClient" + (i + 1), difficulty, gameWidth, gameHeight, maxPlayers, new PartialStatePreference(partialStateWidth, partialStateHeight), gui);
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getToken() {
        return token;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public PartialBoardState getPartialBoardState() {
        return partialBoardState;
    }

    public void setPartialBoardState(PartialBoardState partialBoardState) {
        this.partialBoardState = partialBoardState;
    }
}
