package simulation;

import clients.PlayerClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.FileManager;
import model.PartialStatePreference;
import solvers.RandomSolver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class SimulationManager implements Runnable {

    private final String CONFIGURATION_FILEPATH;
    private final String ipAddress;
    private final int port;

    private long startTime = Integer.MIN_VALUE;
    private long endTime = Integer.MIN_VALUE;
    private SimulationConfig simulationConfiguration;
    private ArrayList<Thread> threads = new ArrayList<>();
    private ArrayList<PlayerClient> players = new ArrayList<>();
    private int playerNameCounter = 1;

    public SimulationManager(String configurationFilepath, String ipAddress, int port) {
        this.CONFIGURATION_FILEPATH = configurationFilepath;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Reads the contents of the simulation config file and parses them into a SimulationConfig object.
     */
    private boolean initialize() {
        if (FileManager.fileIsDirectory(SimulationConfig.SIMULATION_CONFIG_DIR)) {
            final String path = SimulationConfig.SIMULATION_CONFIG_DIR + "/" + CONFIGURATION_FILEPATH;
            if (FileManager.fileExists(path)) {
                String content = null;

                //Read content:
                try {
                    content = FileManager.readFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                //Convert content:
                Gson gson = new Gson();
                try {
                    simulationConfiguration = gson.fromJson(content, SimulationConfig.class);
                }
                catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }
            else {
                System.out.println("File '" + path + "' does not exist.");
                return false;
            }
        }
        else {
            System.out.println("Directory '" + SimulationConfig.SIMULATION_CONFIG_DIR + "' not found.");
            return false;
        }
    }

    @Override
    public void run() {
        if (!initialize()) {
            System.out.println("Failed to properly initialize the simulation.");
            return;
        }

        System.out.println("Simulation '" + CONFIGURATION_FILEPATH + "' started");
        startTime = System.currentTimeMillis();

        while (true) { //TODO REVISE CONDITION TO ALLOW FOR STOP

            final long currentTime = System.currentTimeMillis() - startTime;

            ArrayList<Integer> executedEventIndexes = new ArrayList<>();

            //Check all events and execute them if their time has come up:
            for (int i = 0; i < simulationConfiguration.getEvents().size(); i++) {
                if (currentTime >= simulationConfiguration.getEvents().get(i).getExecutionTime()) {

                    //Log event as executed:
                    executedEventIndexes.add(i);

                    AddPlayersEvent addPlayersEvent = simulationConfiguration.getEvents().get(i);
                    int playersToAdd = (addPlayersEvent.getNumOfPlayersToAdd() > simulationConfiguration.getMaxPlayers() - players.size()) ? simulationConfiguration.getMaxPlayers() - players.size() : addPlayersEvent.getNumOfPlayersToAdd();
                    for (int iP = 0; iP < playersToAdd; iP++) {
                        try {
                            final Socket socket = new Socket(ipAddress, port);
                            PlayerClient playerClient = new PlayerClient(socket, "player" + playerNameCounter, simulationConfiguration.getTimeInterval(), new PartialStatePreference(simulationConfiguration.getClientPartialStateWidth(), simulationConfiguration.getClientPartialStateHeight()), new RandomSolver()); //TODO GENERALIZE
                            players.add(playerClient);
                            System.out.println("[" + currentTime + "] - Player '" + playerClient.getName() + "' added.");
                            Thread thread = new Thread(playerClient, playerClient.getName() + "-Thread");
                            threads.add(thread);
                            thread.start();
                            playerNameCounter++;
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }

                }
            }

            //Remove all executed events:
            for (Integer eventIndex : executedEventIndexes) {
                simulationConfiguration.getEvents().remove((int) eventIndex);
            }

            //Check if all threads have been terminated:
            if (threads.size() > 0) {
                boolean hasNonTerminatedThread = false;
                for (Thread t : threads) {
                    if (t.getState() != Thread.State.TERMINATED) {
                        hasNonTerminatedThread = true;
                        break;
                    }
                }

                if (!hasNonTerminatedThread) {
                    System.out.println("DONE!");
                    return;
                }

            }

        }

    }

    public int getNumberOfActivePlayers() {
        int num = 0;
        for (Thread t : threads) {
            if (t.getState() != Thread.State.TERMINATED) {
                num++;
            }
        }
        return num;
    }

}
