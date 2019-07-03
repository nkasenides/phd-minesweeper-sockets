package simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.FileManager;
import model.Difficulty;

import java.io.IOException;

public class SimulationConfigMaker {

    public static void main(String[] args) {

        //Create config object:
        SimulationConfig config = new SimulationConfig(
                100000,
                1280,
                1280,
                10,
                10,
                10,
                10,
                0,
                Difficulty.EASY
        );

        //Config name:
        final String configFileName = "stresstest.sim";

        //Config events:
        int seconds = 0;
        int addedPlayers = 0;
        final int playersToAdd = 50;
        final int interval = 1000;

        while (addedPlayers < 25000) {
            config.getEvents().add(new AddPlayersEvent(seconds, playersToAdd));
            seconds += interval;
            addedPlayers += playersToAdd;
        }

        //Convert to JSON:
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String configJSON = gson.toJson(config);

        //Check simulation config directory and create it:
        if (!FileManager.fileIsDirectory(SimulationConfig.SIMULATION_CONFIG_DIR)) {
            if (!FileManager.createDirectory(SimulationConfig.SIMULATION_CONFIG_DIR, true)) {
                System.out.println("Failed to create directory '" + SimulationConfig.SIMULATION_CONFIG_DIR + "'.");
                return;
            }
        }

        //Write to file:
        final String path = SimulationConfig.SIMULATION_CONFIG_DIR + "/" + configFileName;
        try {
            FileManager.writeFile(path, configJSON);
            System.out.println("Simulation config file '" + path + "' created successfully.");
        } catch (IOException e) {
            System.out.println("Failed to create config file '" + path + "'.");
            e.printStackTrace();
        }

    }

}
