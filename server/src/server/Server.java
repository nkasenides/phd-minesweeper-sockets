package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import datastore.Datastore;
import exception.InvalidCellReferenceException;
import model.*;
import response.ErrorResponse;
import response.Response;
import response.ResponseStatus;
import services.LocalAdminService;
import services.LocalMasterService;
import services.LocalUserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server implements Runnable {

    public static final int SERVER_PORT = 12345;
    private static final boolean DEBUG = true;
    private static final LocalAdminService ADMIN_SERVICE = new LocalAdminService();
    private static final LocalMasterService MASTER_SERVICE = new LocalMasterService();
    private static final LocalUserService USER_SERVICE = new LocalUserService();

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Socket socket;

    private String clientSessionID;

    public  Server(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            String input;
            while ((input = bufferedReader.readLine()) != null) {

                //Receive request:
                if (DEBUG) System.out.println("Got request by '" + socket.getInetAddress() + ":" + socket.getPort() + "' --> " + input);

                //Parse command from JSON:
                Command receivedCommand = null;
                try {
                    Gson gson = new Gson();
                    receivedCommand = gson.fromJson(input, Command.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                Response response;
                final String endpointName = receivedCommand.getEndpointName();
                final JsonObject payload = receivedCommand.getPayload();

                switch (receivedCommand.getCommandType()) {
                    case ADMIN_SERVICE:
                        final String password = payload.get("password").getAsString();
                        switch (endpointName) {
                            case "createGame":
                                int maxNumOfPlayers = payload.get("maxNumOfPlayers").getAsInt();
                                int width = payload.get("width").getAsInt();
                                int height = payload.get("height").getAsInt();
                                Difficulty difficulty = Difficulty.valueOf(payload.get("difficulty").getAsString());
                                response = ADMIN_SERVICE.createGame(password, maxNumOfPlayers, width, height, difficulty);
                                break;
                            case "startGame":
                                String gameTokenStart = payload.get("gameToken").getAsString();
                                response  = ADMIN_SERVICE.startGame(password, gameTokenStart);
                                break;
                            case "viewGame":
                                String gameTokenView = payload.get("gameToken").getAsString();
                                int partialStateWidth = payload.get("partialStateWidth").getAsInt();
                                int partialStateHeight = payload.get("partialStateHeight").getAsInt();
                                int startRow = payload.get("startRow").getAsInt();
                                int startCol = payload.get("startCol").getAsInt();
                                response = ADMIN_SERVICE.viewGame(password, gameTokenView, partialStateWidth, partialStateHeight, startRow, startCol);
                                break;
                            case "subscribe":
                                String gameTokenSubscribe = payload.get("token").getAsString();
                                int partialStateWidthSubscribe = payload.get("partialStateWidth").getAsInt();
                                int partialStateHeightSubscribe = payload.get("partialStateHeight").getAsInt();
                                response = ADMIN_SERVICE.subscribe(password, gameTokenSubscribe, partialStateWidthSubscribe, partialStateHeightSubscribe);
                                if (response.getStatus() == ResponseStatus.OK) {
                                    clientSessionID = response.getData().get("sessionID").getAsString();
                                }
                                break;
                            default:
                                response = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                break;
                        }
                        break;
                    case MASTER_SERVICE:
                        switch (endpointName) {
                            case "listGames":
                                response = MASTER_SERVICE.listGames();
                                break;
                            case "join":
                                String token = payload.get("token").getAsString();
                                String playerName = payload.get("playerName").getAsString();
                                int partialStateWidth = payload.get("partialStateWidth").getAsInt();
                                int partialStateHeight = payload.get("partialStateHeight").getAsInt();
                                response = MASTER_SERVICE.join(token, playerName, partialStateWidth, partialStateHeight);
                                if (response.getStatus() == ResponseStatus.OK) {
                                    clientSessionID = response.getData().get("sessionID").getAsString();
                                }
                                break;
                            default:
                                response = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                break;
                        }
                        break;
                    case USER_SERVICE:
                        final String sessionID = payload.get("sessionID").getAsString();
                        switch (endpointName) {
                            case "getPartialState":
                                response = USER_SERVICE.getPartialState(sessionID);
                                break;
                            case "move":
                                final int row = payload.get("row").getAsInt();
                                final int col = payload.get("col").getAsInt();
                                response = USER_SERVICE.move(sessionID, row, col);
                                break;
                            case "reveal":
                                int rowReveal = payload.get("row").getAsInt();
                                int colReveal = payload.get("col").getAsInt();
                                response = USER_SERVICE.reveal(sessionID, rowReveal, colReveal);
                                break;
                            case "flag":
                                int rowFlag = payload.get("row").getAsInt();
                                int colFlag = payload.get("col").getAsInt();
                                response = USER_SERVICE.flag(sessionID, rowFlag, colFlag);
                                break;
                            default:
                                response = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                break;
                        }
                        break;
                    default:
                        response = new ErrorResponse("Invalid service", "The service '" + receivedCommand.getCommandType() + "' is not a valid service.");
                        break;
                }

                printWriter.println(response.toJSON());
                printWriter.flush();

            }
        } catch (SocketTimeoutException e) {
            System.out.println("Client disconnected.");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public static ArrayList<Server> serverInstances = new ArrayList<>();

    /**
     * Find when serverInstances need an update and send the appropriate partial game state back to them.
     * This method should be called just before returning from a state-changing API call such as reveal & flag.
     * updaterSessionID --> do not re-update the player who initiated the update.
     */
    public static void updateClients(String gameToken, String updaterSessionID) {
        for (Server serverInstance : serverInstances) {
            Session session = Datastore.getSession(serverInstance.clientSessionID);
            if (session == null) throw new RuntimeException("The server instance has an invalid session ID '" + serverInstance.clientSessionID + "'.");
            if (session.getGameToken().equals(gameToken) && !session.getSessionID().equals(updaterSessionID)) {

                //Get the referenced game:
                Game game = Datastore.getGame(gameToken);

                //Get the partial state for this session:
                PartialStatePreference partialStatePreference = session.getPartialStatePreference();
                PartialBoardState partialBoardState;
                try {
                    partialBoardState = new PartialBoardState(partialStatePreference.getWidth(), partialStatePreference.getHeight(), session.getPositionCol(), session.getPositionRow(), game.getFullBoardState());
                }
                catch (InvalidCellReferenceException e) {
                    throw new RuntimeException(e.getMessage());
                }

                //Send the update in format:
                /*
                    ...
                    "payload": {
                        "gameState": ...
                        "partialBoardState": ...
                    }
                 */

                Gson gson = new Gson();
                JsonObject payload = new JsonObject();
                payload.add("gameState", gson.toJsonTree(game.getGameState()));
                payload.add("partialBoardState", gson.toJsonTree(partialBoardState));
                Command command = new Command(CommandType.CLIENT_UPDATE_SERVICE, "", payload);
                String commandJSON = gson.toJson(command);
                serverInstance.getPrintWriter().println(commandJSON);
                serverInstance.getPrintWriter().flush();
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Starting server...");
            System.out.println("Creating server socket at: " + SERVER_PORT);
            final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Socket created, port " + SERVER_PORT);

            while (true) {
                if (DEBUG) System.out.println("Waiting for inbound connection...");
                Socket socket = serverSocket.accept();
                if (DEBUG) System.out.println("New connection from: " + socket.getInetAddress() + ":" + socket.getPort());
                Server server = new Server(socket);
                serverInstances.add(server);
                new Thread(server).start();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
