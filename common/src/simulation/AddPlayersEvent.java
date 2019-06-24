package simulation;

public class AddPlayersEvent extends SimulationEvent {

    private int numOfPlayersToAdd;

    private AddPlayersEvent() {
        super(SimulationEventType.ADD_PLAYERS_EVENT, 0);
    }

    public AddPlayersEvent(long executionTime, int numOfPlayersToAdd) {
        super(SimulationEventType.ADD_PLAYERS_EVENT, executionTime);
        this.numOfPlayersToAdd = numOfPlayersToAdd;
    }

    public int getNumOfPlayersToAdd() {
        return numOfPlayersToAdd;
    }

    public void setNumOfPlayersToAdd(int numOfPlayersToAdd) {
        this.numOfPlayersToAdd = numOfPlayersToAdd;
    }

}