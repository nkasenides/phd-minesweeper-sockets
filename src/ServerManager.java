//import java.util.HashMap;
//
//public class ServerManager {
//
//    private static final HashMap<String, Server> SERVERS = new HashMap<>();
//
//    public static boolean serverExists(String name) {
//        return (SERVERS.get(name) != null);
//    }
//
//    public static void registerServer(Server server) {
//        SERVERS.putIfAbsent(server.getName(), server);
//    }
//
//    public static Server findServer(String name) {
//        return SERVERS.get(name);
//    }
//
//}
