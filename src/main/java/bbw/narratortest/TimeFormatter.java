package bbw.narratortest;

public class TimeFormatter {
    public static String formatMilisString(long totalMilisLong) {
        int totalSeconds = ((int)totalMilisLong) / 1000;
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder timeString = new StringBuilder();
        
        if (days > 0) {
            timeString.append(days).append(" day").append(days > 1 ? "s" : "").append(", ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(", ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(", ");
        }
        if (seconds > 0 || totalSeconds == 0) {
            timeString.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
        }

        return timeString.toString();
    }
}
