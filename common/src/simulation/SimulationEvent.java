package simulation;

public abstract class SimulationEvent {

    private SimulationEventType eventType;
    private long executionTime;

    private SimulationEvent() { }

    public SimulationEvent(SimulationEventType eventType, long executionTime) {
        this.eventType = eventType;
        this.executionTime = executionTime;
    }

    public SimulationEventType getEventType() {
        return eventType;
    }

    public long getExecutionTime() {
        return executionTime;
    }

}
