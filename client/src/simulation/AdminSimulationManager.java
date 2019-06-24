package simulation;

import clients.AdminClient;
import clients.PlayerClient;
import model.PartialStatePreference;
import solvers.RandomSolver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class AdminSimulationManager extends SimulationManager<AdminClient> implements Runnable {

    public AdminSimulationManager(String configurationFilepath, String ipAddress, int port) {
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

        final int numOfClients = 1;
        for (int i = 0; i < numOfClients; i++) {
            try {
                final Socket socket = new Socket(ipAddress, port);
                AdminClient adminClient = new AdminClient(socket, "admin" + nameCounter, simulationConfiguration.getDifficulty(), simulationConfiguration.getGameWidth(), simulationConfiguration.getGameHeight(), simulationConfiguration.getMaxPlayers(), new PartialStatePreference(simulationConfiguration.getClientPartialStateWidth(), simulationConfiguration.getClientPartialStateHeight()));
                instances.add(adminClient);
                System.out.println("Admin '" + adminClient.getName() + "' added.");
                Thread thread = new Thread(adminClient, adminClient.getName() + "-Thread");
                threads.add(thread);
                thread.start();
                nameCounter++;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }

        while (true) {
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

}
