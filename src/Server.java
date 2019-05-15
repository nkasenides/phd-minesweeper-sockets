//import java.net.*;
//import java.io.*;
//
//public class Server implements Runnable {
//
//    private ServerSocket serverSocket;
//    private final String name;
//    private final int port;
//
//    public Server(String name, int port) {
//        this.name = name;
//        this.port = port;
//    }
//
//    public void initialize() {
//        try {
//            serverSocket = new ServerSocket(port);
//            serverSocket.setSoTimeout(0); //TODO May be unnecessary
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void run() {
//        while(true) {
//            try {
//
//                //Input:
//                Socket server = serverSocket.accept();
//                printMessage(server.getRemoteSocketAddress() + " connected");
//                InputStreamReader in = new InputStreamReader(server.getInputStream());
//                BufferedReader bufferedReader = new BufferedReader(in);
//                String input = bufferedReader.readLine();
//
//                printMessage("Client says: '" + input + "'");//TODO Interpret commands...
//
//                //Output:
//                PrintWriter printWriter = new PrintWriter(server.getOutputStream());
//                printWriter.println("Hello from " + name);
//                printWriter.flush();
//
//            } catch (SocketTimeoutException s) {
//                printMessage("Socket timed out");
//                break;
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            }
//        }
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public int getPort() {
//        return port;
//    }
//
//    private void printMessage(String message) {
//        System.out.println("[" + name + "] --> " + message);
//    }
//
//}