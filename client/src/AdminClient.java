import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.Command;
import model.CommandType;
import model.Difficulty;
import response.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static response.ResponseStatus.OK;

public class AdminClient implements Runnable {

    public static final int SERVER_PORT = 12345;
    public static final long DELAY = 1000L;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String name;

    public AdminClient(Socket socket, String name) throws IOException {
        this.name = name;
        printWriter = new PrintWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {

        String token = null;

        try {
//            while (true) {

            //--Create game:
            {

                System.out.println("Creating game...");

                //Create request object:
                JsonObject object = new JsonObject();
                object.addProperty("maxNumOfPlayers", "5");
                object.addProperty("difficulty", Difficulty.HARD.toString());
                object.addProperty("width", 100);
                object.addProperty("height", 100);
                object.addProperty("password", "1234");
                Command command = new Command(CommandType.ADMIN_SERVICE, "createGame", object);

//                System.out.println("Sending 'hello'");
//                printWriter.println("hello(" + name + ")");

                //Convert to JSON and send:
                Gson gson = new Gson();
                String commandJSON = gson.toJson(command);
                printWriter.println(commandJSON);
//                System.out.println("Sending request --> " + commandJSON);
                printWriter.flush();

                //Wait for and print reply:
//                System.out.println("Waiting for reply...");
                String reply = bufferedReader.readLine();
//                System.out.println("Got: '" + reply + "'");

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

//                System.out.println("Sending 'hello'");
//                printWriter.println("hello(" + name + ")");

                //Convert to JSON and send:
                Gson gson = new Gson();
                String commandJSON = gson.toJson(command);
                printWriter.println(commandJSON);
//                System.out.println("Sending request --> " +commandJSON);
                printWriter.flush();

                //Wait for and print reply:
//                System.out.println("Waiting for reply...");
                String reply = bufferedReader.readLine();
//                System.out.println("Got: '" + reply + "'");

                Response startResponse = gson.fromJson(reply, Response.class);
                if (startResponse.getStatus() == OK) {
                    System.out.println("Game with token '" + token + "' started.");
                }
            }

                //Delay:
//                Thread.sleep(DELAY);
//            }
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        int numOfClients = 1;
        AdminClient[] clients = new AdminClient[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket("localhost", SERVER_PORT);
                clients[i] = new AdminClient(socket, "name-" + i);
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
