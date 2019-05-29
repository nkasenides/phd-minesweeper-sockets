package simulation;

public class MemoryMeasurement {
    private long timestamp;
    private long memoryConsumption;

    public MemoryMeasurement(long timestamp, long memoryConsumption) {
        this.timestamp = timestamp;
        this.memoryConsumption = memoryConsumption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getMemoryConsumption() {
        return memoryConsumption;
    }

    public void setMemoryConsumption(long memoryConsumption) {
        this.memoryConsumption = memoryConsumption;
    }
}