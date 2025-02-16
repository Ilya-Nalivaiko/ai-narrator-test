package bbw.narratortest;

// Event class to represent individual events
public class Event {
    private String type;
    private String extra;
    private long timestamp;

    public Event(String type, String extra, long timestamp) {
        this.type = type;
        this.extra = extra;
        this.timestamp = timestamp - NarratorTest.startTime;
    }

    public String getType() {
        return type;
    }

    public String getExtra() {
        return extra;
    }

    public long getTimestamp() {
        return timestamp;
    }
}