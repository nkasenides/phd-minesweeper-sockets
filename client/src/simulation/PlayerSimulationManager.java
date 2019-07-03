package simulation;

import clients.PlayerClient;
import model.PartialStatePreference;
import solvers.RandomSolver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class PlayerSimulationManager extends SimulationManager<PlayerClient> implements Runnable {

    private static final boolean SHOW_PROGRESS = true;
    public static final int PROGRESS_TIME = 5000; //Show progress every 5 seconds.
    private long delay = 0;
    private long lastTickTime = 0;
    private boolean progressSymbol = true;
    private int progressLineWidth = 0;

    public PlayerSimulationManager(String configurationFilepath, String ipAddress, int port) {
        super(configurationFilepath, ipAddress, port);
    }

    @Override
    public void run() {

        if (!initialize()) {
            System.out.println("Failed to properly initialize the simulation.");
            return;
        }

        System.out.println("Simulation '" + CONFIGURATION_FILEPATH + "' started" + System.lineSeparator());

        startTime = System.currentTimeMillis();

        while (true) {

            //Time management:
            final long currentTime = System.currentTimeMillis() - startTime;

            if (SHOW_PROGRESS) {
                delay += currentTime - lastTickTime;
                if (delay >= PROGRESS_TIME) {
                    if (lastTickTime != 0) {
                        if (progressSymbol) {
                            System.out.print("+");
                        } else {
                            System.out.print("O");
                        }
                        progressSymbol = !progressSymbol;
                        progressLineWidth++;
                        if (progressLineWidth % 80 == 0) System.out.println();
                    }
                    delay = 0;
                }
                lastTickTime = currentTime;
            }


            ArrayList<Integer> executedEventIndexes = new ArrayList<>();

            //Check all events and execute them if their time is <= current time:
            for (int i = 0; i < simulationConfiguration.getEvents().size(); i++) {
                if (currentTime >= simulationConfiguration.getEvents().get(i).getExecutionTime()) {

                    //Log event as executed:
                    executedEventIndexes.add(i);

                    AddPlayersEvent addPlayersEvent = simulationConfiguration.getEvents().get(i);
                    final ArrayList<Thread> currentThreadAddList = new ArrayList<>();
                    int playersToAdd = (addPlayersEvent.getNumOfPlayersToAdd() > simulationConfiguration.getMaxPlayers() - instances.size()) ? simulationConfiguration.getMaxPlayers() - instances.size() : addPlayersEvent.getNumOfPlayersToAdd();
                    for (int iP = 0; iP < playersToAdd; iP++) {
                        try {
                            final Socket socket = new Socket(ipAddress, port);
                            PlayerClient playerClient = new PlayerClient(socket, "player" + nameCounter, simulationConfiguration.getTimeInterval(), new PartialStatePreference(simulationConfiguration.getPlayerPartialStateWidth(), simulationConfiguration.getPlayerPartialStateHeight()), new RandomSolver()); //TODO GENERALIZE
                            instances.add(playerClient);
                            System.out.println("[" + currentTime + "] - Player '" + playerClient.getName() + "' added.");
                            Thread thread = new Thread(playerClient, playerClient.getName() + "-Thread");
                            threads.add(thread);
                            currentThreadAddList.add(thread);
                            nameCounter++;
                        } catch (IOException ioe) {
                            System.out.println("Failed to instantiate player " + nameCounter + ". The backlog is probably full, waiting...");
                        }
                    }

                    for (final Thread t : currentThreadAddList) {
                        t.start();
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
