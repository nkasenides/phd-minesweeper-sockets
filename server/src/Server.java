import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import model.Command;
import model.Difficulty;
import model.Direction;
import response.ErrorResponse;
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

public class Server implements Runnable {

    public static final int SERVER_PORT = 12345;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Socket socket;

    private static final LocalAdminService ADMIN_SERVICE = new LocalAdminService();
    private static final LocalMasterService MASTER_SERVICE = new LocalMasterService();
    private static final LocalUserService USER_SERVICE = new LocalUserService();

    public Server(Socket socket) throws IOException {
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
                System.out.println("Got request by '" + socket.getInetAddress() + ":" + socket.getPort() + "' --> " + input);

                //Parse command from JSON:
                Command receivedCommand = null;
                try {
                    Gson gson = new Gson();
                    receivedCommand = gson.fromJson(input, Command.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                String reply;
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
                                reply = ADMIN_SERVICE.createGame(password, maxNumOfPlayers, width, height, difficulty);
                                break;
                            case "startGame":
                                String gameTokenStart = payload.get("gameToken").getAsString();
                                reply = ADMIN_SERVICE.startGame(password, gameTokenStart);
                                break;
                            case "viewGame":
                                String gameTokenView = payload.get("gameToken").getAsString();
                                int partialStateWidth = payload.get("partialStateWidth").getAsInt();
                                int partialStateHeight = payload.get("partialStateHeight").getAsInt();
                                int startX = payload.get("startX").getAsInt();
                                int startY = payload.get("startY").getAsInt();
                                reply = ADMIN_SERVICE.viewGame(password, gameTokenView, partialStateWidth, partialStateHeight, startX, startY);
                                break;
                            default:
                                ErrorResponse errorResponse = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                reply = errorResponse.toJSON();
                                break;
                        }
                        break;
                    case MASTER_SERVICE:
                        switch (endpointName) {
                            case "listGames":
                                reply = MASTER_SERVICE.listGames();
                                break;
                            case "join":
                                String token = payload.get("token").getAsString();
                                String playerName = payload.get("playerName").getAsString();
                                int partialStateWidth = payload.get("partialStateWidth").getAsInt();
                                int partialStateHeight = payload.get("partialStateHeight").getAsInt();
                                reply = MASTER_SERVICE.join(token, playerName, partialStateWidth, partialStateHeight);
                                break;
                            default:
                                ErrorResponse errorResponse = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                reply = errorResponse.toJSON();
                                break;
                        }
                        break;
                    case USER_SERVICE:
                        final String sessionID = payload.get("sessionID").getAsString();
                        switch (endpointName) {
                            case "getPartialState":
                                reply = USER_SERVICE.getPartialState(sessionID);
                                break;
                            case "move":
                                Direction direction = Direction.valueOf(payload.get("direction").getAsString());
                                int unitOfMovement = payload.get("unitOfMovement").getAsInt();
                                reply = USER_SERVICE.move(sessionID, direction, unitOfMovement);
                                break;
                            case "reveal":
                                int xReveal = payload.get("x").getAsInt();
                                int yReveal = payload.get("y").getAsInt();
                                reply = USER_SERVICE.reveal(sessionID, xReveal, yReveal);
                                break;
                            case "flag":
                                int xFlag = payload.get("x").getAsInt();
                                int yFlag = payload.get("y").getAsInt();
                                reply = USER_SERVICE.flag(sessionID, xFlag, yFlag);
                                break;
                            default:
                                ErrorResponse errorResponse = new ErrorResponse("Invalid endpoint", "The endpoint '" + endpointName + "' is not valid.");
                                reply = errorResponse.toJSON();
                                break;
                        }
                        break;
                    default:
                        ErrorResponse errorResponse = new ErrorResponse("Invalid service", "The service '" + receivedCommand.getCommandType() + "' is not a valid service.");
                        reply = errorResponse.toJSON();
                        break;
                }

                printWriter.println(reply);
                printWriter.flush();

            }
        } catch (SocketTimeoutException e) {
            System.out.println("Client disconnected.");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args) {
        try {

            System.out.println("Starting server...");
            System.out.println("Creating server socket at: " + SERVER_PORT);
            final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Socket created, port " + SERVER_PORT);

            while(true) {
                System.out.println("Waiting for inbound connection...");
                Socket socket = serverSocket.accept();
                System.out.println("New connection from: " + socket.getInetAddress() + ":" + socket.getPort());
                new Thread(new Server(socket)).start();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
