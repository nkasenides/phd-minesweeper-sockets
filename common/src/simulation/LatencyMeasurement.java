package simulation;

public class LatencyMeasurement {

    private long timestamp;
    private long latency;

    public LatencyMeasurement(long timestamp, long latency) {
        this.timestamp = timestamp;
        this.latency = latency;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public long getLatency() {
        return latency;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

}