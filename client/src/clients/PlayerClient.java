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
                JsonElement gameStateElement = joinResponse.getData().get("gameState");
                gameState = gson.fromJson(gameStateElement, GameState.class);
                JsonElement partialBoardStateElement = joinResponse.getData().get("partialBoardState");
                partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
                System.out.println(name + ": Joined game with token '" + games.get(0).getToken() + " with session ID '" + sessionID + "'.");
            }

            System.out.println(name + ": Starting to play!");

            //While the game is not over, keep making moves:
            while (true) {

                System.out.println();
                System.out.println();

                Command command = makeMove();
                System.out.println("[" + name + "]: Decided to make move '" + command.getEndpointName() + "' at cell (" +
                        command.getPayload().get("row").getAsInt() + "," + command.getPayload().get("col").getAsInt() + ")");

                //Convert to JSON and send:
                String moveCommandJSON = gson.toJson(command);
                printWriter.println(moveCommandJSON);
                printWriter.flush();

                //Wait for and print reply:
                String reply = bufferedReader.readLine();
                System.out.println("[" + name + "]: " + reply);

                //Parse reply and refresh the local state, if a response from server:
                Response playResponse = gson.fromJson(reply, Response.class);
                if (playResponse != null) {
                    if (playResponse.getStatus() == OK) {
                        JsonElement partialBoardStateElement = playResponse.getData().get("partialBoardState");
                        JsonElement gameStateElement = playResponse.getData().get("gameState");
                        this.gameState = gson.fromJson(gameStateElement, GameState.class);
                        this.partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
                    }
                }
                //Or get command from server and update the state:
                else {
                    Command updateCommand = gson.fromJson(reply, Command.class);
                    if (updateCommand.getCommandType() != null) {
                        if (updateCommand.getCommandType() == CommandType.CLIENT_UPDATE_SERVICE) {
                            JsonObject payload = updateCommand.getPayload();
                            JsonElement gameStateElement = payload.get("gameState");
                            JsonElement partialBoardStateElement = payload.get("partialBoardState");
                            this.gameState = gson.fromJson(gameStateElement, GameState.class);
                            this.partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
                        }
                    }
                }

                if (gameState.isEnded()) {
                    System.out.println(name + ": GAME ENDED (" + gameState + ")");
                    return;
                }

                if (turnInterval > 0) Thread.sleep(turnInterval);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private Command makeMove() {
        //Scan all the cells from partial state and save those who are RevealState.COVERED:
        class UnrevealedCell {
            public int row;
            public int col;
            public UnrevealedCell(int row, int col) { this.row = row; this.col = col; }
        }
        ArrayList<UnrevealedCell> unrevealedCells = new ArrayList<>();
        for (int row = 0; row < partialBoardState.getCells().length; row++) {
            for (int col = 0; col < partialBoardState.getCells()[row].length; col++) {
                if (partialBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
                    unrevealedCells.add(new UnrevealedCell(row, col));
                }
            }
        }

        //Command attributes:
        JsonObject object = new JsonObject();
        object.addProperty("sessionID", sessionID);

        //Check if there are any unrevealed cells, if not then the player has to shift position:
        if (unrevealedCells.size() < 1) {

            final String moveEndpoint = "move";
            final CommandType commandType = CommandType.USER_SERVICE;

            System.out.println("*** NO MORE UNREVEALED CELLS IN RANGE (" + partialBoardState.getStartingRow() + "," + partialBoardState.getStartingCol() +
                    ") to (" + (partialBoardState.getStartingRow() + partialBoardState.getHeight()) + "," + (partialBoardState.getStartingCol() + partialBoardState.getWidth()) + ")***");

            final int cellsRight = gameWidth - (partialBoardState.getStartingCol() + partialBoardState.getWidth());

            if (cellsRight >= partialBoardState.getWidth()) {
                object.addProperty("row", partialBoardState.getStartingRow());
                object.addProperty("col", partialBoardState.getStartingCol() + partialBoardState.getWidth());
                return new Command(commandType, moveEndpoint, object);
            }
            else if (cellsRight > 0) {
                object.addProperty("row", partialBoardState.getStartingRow());
                object.addProperty("col", partialBoardState.getStartingCol() + cellsRight);
                return new Command(commandType, moveEndpoint, object);
            }
            else {
                final int cellsDown = gameHeight - (partialBoardState.getStartingRow() + partialBoardState.getHeight());
                if (cellsDown >= partialBoardState.getHeight()) {
                    object.addProperty("row", partialBoardState.getStartingRow() + partialBoardState.getHeight());
                    object.addProperty("col", 0);
                    return new Command(commandType, moveEndpoint, object);
                }
                else {
                    object.addProperty("row", partialBoardState.getStartingRow() + cellsDown);
                    object.addProperty("col", 0);
                    return new Command(commandType, moveEndpoint, object);
                }
            }
        }

        //Otherwise, select a random cell from unrevealedCells with a random move and play it:
        else {

            System.out.println("*** FOUND " + unrevealedCells.size() + " UNREVEALED CELLS IN RANGE (" + partialBoardState.getStartingRow() + "," + partialBoardState.getStartingCol() +
                    ") to (" + (partialBoardState.getStartingRow() + partialBoardState.getHeight()) + "," + (partialBoardState.getStartingCol() + partialBoardState.getWidth()) + ")***");

            //If there are unrevealed cells, choose a random one out of the list:
            Random random = new Random();
            int randomCellIndex = random.nextInt(unrevealedCells.size());
            int randomRow = unrevealedCells.get(randomCellIndex).row + partialBoardState.getStartingRow();
            int randomCol = unrevealedCells.get(randomCellIndex).col + partialBoardState.getStartingCol();

            //Choose which move to make. Currently a 60% reveal vs 40% flag chance.
//            moveEndpoint = random.nextInt(10) > 6 ? "flag" : "reveal";
            final String moveEndpoint = "reveal"; //TODO CHANGE

            //Remove the cell from the unrevealedCells:
            unrevealedCells.remove(randomCellIndex);

            //Package the move into a command, convert to JSON and send:
            object.addProperty("row", randomRow);
            object.addProperty("col", randomCol);
            object.addProperty("sessionID", sessionID);
            return new Command(CommandType.USER_SERVICE, moveEndpoint, object);
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

