import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    public static final int SERVER_PORT = 12345;
    public static final long DELAY = 1000L;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private String name;

    public Client(Socket socket, String name) throws IOException {
        this.name = name;
        printWriter = new PrintWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                // todo do request
                System.out.println("Sending 'hello'");
                printWriter.println("hello(" + name + ")");
                printWriter.flush();
                System.out.println("Waiting for reply...");
                String reply = bufferedReader.readLine();
                System.out.println("Got: '" + reply + "'");

                Thread.sleep(DELAY);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        int numOfClients = 10;
        Client [] clients = new Client[numOfClients];
        for(int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket("localhost", SERVER_PORT);
                clients[i] = new Client(socket, "name-" + i);
                new Thread(clients[i]).start();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
