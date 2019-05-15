//import java.net.*;
//import java.io.*;
//
//public class Client {
//
//    private final String name;
//    private final String serverName;
//
//    public Client(String clientName, String serverName) {
//        this.name = clientName;
//        this.serverName = serverName;
//    }
//
//    public void connect() {
//        try {
//            //Find the server:
//            printMessage("Finding server '" + serverName + "'...");
//            Server server = ServerManager.findServer(serverName);
//            if (server == null) {
//                System.out.println("Server '" + serverName + "' not found. Aborting.");
//                return;
//            }
//
//            //Connect:
//            printMessage("Connecting to " + serverName + " on port " + server.getPort());
//            Socket client = new Socket("localhost", server.getPort()); //TODO needs to change for multiple servers located at different IPs
//            printMessage("Connected to " + serverName + "@" + client.getRemoteSocketAddress());
//
//            //Output:
//            PrintWriter printWriter = new PrintWriter(client.getOutputStream());
//            printWriter.println("Hello from " + name);
//            printWriter.flush();
//
//            //Input:
//            InputStreamReader in = new InputStreamReader(client.getInputStream());
//            BufferedReader bufferedReader = new BufferedReader(in);
//            String input = bufferedReader.readLine();
//            printMessage("Server says: '" + input + "'");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    private void printMessage(String message) {
//        System.out.println("[" + name + "] --> " + message);
//    }
//
//}