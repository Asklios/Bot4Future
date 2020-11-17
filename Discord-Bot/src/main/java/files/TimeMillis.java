package main.java.files;

public class TimeMillis {

    public static long monthMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("m", ""));
        long dayMillis = 1000*60*60*24;
        long monthMillis = dayMillis*30;
        return timeInt*monthMillis;
    }

    public static long weekMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("w", ""));
        long weekMillis = 1000*60*60*24*7;
        return timeInt*weekMillis;
    }

    public static long dayMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("d", ""));
        long dayMillis = 1000*60*60*24;
        return timeInt*dayMillis;
    }

    public static long hourMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("h", ""));
        long hourMillis = 1000*60*60;
        return timeInt*hourMillis;
    }

    public static long minuteMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("min", ""));
        long minuteMillis = 1000*60;
        return timeInt*minuteMillis;
    }

    public static long secondMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("sec", ""));
        long secondMillis = 1000;
        return timeInt*secondMillis;
    }

    public static String upTime(long millis) {
        long secondsUptime = millis / 1000;
        long minutesUptime = ((secondsUptime % 86400) % 3600) / 60;
        long hoursUptime = (secondsUptime % 86400) / 3600;
        long daysUptime = secondsUptime / 86400;

        return (daysUptime == 0 ? "" : daysUptime + "d:") +
                (hoursUptime == 0 ? "" : hoursUptime + "h:") +
                minutesUptime + "min:" + secondsUptime + "sec";
    }
}
