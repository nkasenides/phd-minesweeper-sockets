import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    public static final int SERVER_PORT = 12345;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Server(Socket socket) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            String input;
            while((input = bufferedReader.readLine()) != null) {
                String reply = ">>" + input;
                printWriter.println(reply);
                printWriter.flush();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Creating server socket at: " + SERVER_PORT);
            final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            while(true) {
                System.out.println("Waiting for inbound connection...");
                Socket socket = serverSocket.accept();
                System.out.println("New connection from: " + socket);
                new Thread(new Server(socket)).start();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
