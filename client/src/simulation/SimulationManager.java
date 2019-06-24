package simulation;

import clients.AdminClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.FileManager;

import java.io.IOException;
import java.util.ArrayList;

public abstract class SimulationManager <ClientType> {

    protected final String CONFIGURATION_FILEPATH;
    protected final String ipAddress;
    protected final int port;

    protected long startTime = Integer.MIN_VALUE;
    protected long endTime = Integer.MIN_VALUE;
    protected SimulationConfig simulationConfiguration;
    protected ArrayList<Thread> threads = new ArrayList<>();
    protected ArrayList<ClientType> instances = new ArrayList<>();
    protected int nameCounter = 1;

    public SimulationManager(String configurationFilepath, String ipAddress, int port) {
        this.CONFIGURATION_FILEPATH = configurationFilepath;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getCONFIGURATION_FILEPATH() {
        return CONFIGURATION_FILEPATH;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    /**
     * Reads the contents of the simulation config file and parses them into a SimulationConfig object.
     */
    protected boolean initialize() {
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

}
