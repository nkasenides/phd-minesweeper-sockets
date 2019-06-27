package simulation;

import clients.PlayerClient;
import model.PartialStatePreference;
import solvers.RandomSolver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class PlayerSimulationManager extends SimulationManager<PlayerClient> implements Runnable {

    public PlayerSimulationManager(String configurationFilepath, String ipAddress, int port) {
        super(configurationFilepath, ipAddress, port);
    }

    @Override
    public void run() {

        if (!initialize()) {
            System.out.println("Failed to properly initialize the simulation.");
            return;
        }

        System.out.println("Simulation '" + CONFIGURATION_FILEPATH + "' started");
        startTime = System.currentTimeMillis();

        while (true) {

            final long currentTime = System.currentTimeMillis() - startTime;

            ArrayList<Integer> executedEventIndexes = new ArrayList<>();

            //Check all events and execute them if their time has come up:
            for (int i = 0; i < simulationConfiguration.getEvents().size(); i++) {
                if (currentTime >= simulationConfiguration.getEvents().get(i).getExecutionTime()) {

                    //Log event as executed:
                    executedEventIndexes.add(i);

                    AddPlayersEvent addPlayersEvent = simulationConfiguration.getEvents().get(i);
                    int playersToAdd = (addPlayersEvent.getNumOfPlayersToAdd() > simulationConfiguration.getMaxPlayers() - instances.size()) ? simulationConfiguration.getMaxPlayers() - instances.size() : addPlayersEvent.getNumOfPlayersToAdd();
                    for (int iP = 0; iP < playersToAdd; iP++) {
                        try {
                            final Socket socket = new Socket(ipAddress, port);
                            PlayerClient playerClient = new PlayerClient(socket, "player" + nameCounter, simulationConfiguration.getTimeInterval(), new PartialStatePreference(simulationConfiguration.getPlayerPartialStateWidth(), simulationConfiguration.getPlayerPartialStateHeight()), new RandomSolver()); //TODO GENERALIZE
                            instances.add(playerClient);
//                            System.out.println("[" + currentTime + "] - Player '" + playerClient.getName() + "' added.");
                            Thread thread = new Thread(playerClient, playerClient.getName() + "-Thread");
                            threads.add(thread);
                            thread.start();
                            nameCounter++;
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
