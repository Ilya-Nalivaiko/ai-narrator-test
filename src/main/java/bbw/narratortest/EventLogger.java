package bbw.narratortest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// EventLogger class to manage the list of events
public class EventLogger {
    private List<Event> events;

    public EventLogger() {
        events = new ArrayList<>();
    }

    // Function to append a new event to the list
    public void appendEvent(String type, String extra, long timestamp) {
        events.add(new Event(type, extra, timestamp));
    }

    // Function to collapse events of the same type and extra into one event with a count
    public String collapseEvents() {
        // Use a map to group events by type and extra
        Map<String, Map<String, Integer>> eventMap = new HashMap<>();

        for (Event event : events) {
            String type = event.getType();
            String extra = event.getExtra();

            // Initialize the inner map if it doesn't exist
            eventMap.putIfAbsent(type, new HashMap<>());

            // Increment the count for the specific type and extra
            eventMap.get(type).put(extra, eventMap.get(type).getOrDefault(extra, 0) + 1);
        }

        // Build the result string
        StringBuilder result = new StringBuilder();
        result.append("Recently, the player performed the following actions:");
        for (Map.Entry<String, Map<String, Integer>> typeEntry : eventMap.entrySet()) {
            String type = typeEntry.getKey();
            Map<String, Integer> extraMap = typeEntry.getValue();

            for (Map.Entry<String, Integer> extraEntry : extraMap.entrySet()) {
                String extra = extraEntry.getKey();
                int count = extraEntry.getValue();

                result.append(String.format("[%s] a [%s]: x%d%n", type, extra, count));
            }
        }

        events = new ArrayList<>();

        return result.toString();
    }

    public void clear(){
        events = new ArrayList<>();
    }
}