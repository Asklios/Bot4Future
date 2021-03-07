package main.java.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class for converting milliseconds to string or the other way round.
 *
 * @author Asklios
 * @version 08.12.2020
 */

public class TimeMillis {

    /**
     * Converts the number of months to a long value of milliseconds.
     * @param timeString 1m corresponds to one month.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long monthMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("m", ""));
        long dayMillis = 1000*60*60*24;
        long monthMillis = dayMillis*30;
        return timeInt*monthMillis;
    }

    /**
     * Converts the number of weeks to a long value of milliseconds.
     * @param timeString 1w corresponds to one week.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long weekMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("w", ""));
        long weekMillis = 1000*60*60*24*7;
        return timeInt*weekMillis;
    }

    /**
     * Converts the number of days to a long value of milliseconds.
     * @param timeString 1d corresponds to one day.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long dayMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("d", ""));
        long dayMillis = 1000*60*60*24;
        return timeInt*dayMillis;
    }

    /**
     * Converts the number of hours to a long value of milliseconds.
     * @param timeString 1h corresponds to one hour.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long hourMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("h", ""));
        long hourMillis = 1000*60*60;
        return timeInt*hourMillis;
    }

    /**
     * Converts the number of minutes to a long value of milliseconds.
     * @param timeString 1min corresponds to one minute.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long minuteMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("min", ""));
        long minuteMillis = 1000*60;
        return timeInt*minuteMillis;
    }

    /**
     * Converts the number of seconds to a long value of milliseconds.
     * @param timeString 1sec corresponds to one second.
     * @return The milliseconds that correspond to the specified time.
     * @exception NumberFormatException if the string unexpectedly contains characters other than numbers, besides the timeunit.
     */
    public static long secondMillis(String timeString) throws NumberFormatException {
        long timeInt = Long.parseLong(timeString.replace("sec", ""));
        long secondMillis = 1000;
        return timeInt*secondMillis;
    }

    /**
     * Converts a long value of milliseconds to a string of days:hours:minutes:seconds.
     * @param millis the long value of milliseconds.
     * @return days:hours:minutes:seconds corresponding to the specified milliseconds.
     */
    public static String upTime(long millis) {
        long secondsUptime = millis / 1000;
        long minutesUptime = ((secondsUptime % 86400) % 3600) / 60;
        long hoursUptime = (secondsUptime % 86400) / 3600;
        long daysUptime = secondsUptime / 86400;

        return (daysUptime == 0 ? "" : daysUptime + "d:") +
                (hoursUptime == 0 ? "" : hoursUptime + "h:") +
                minutesUptime + "min:" + secondsUptime + "sec";
    }

    /**
     * Converts a String like 1w to the long value of milliseconds in a week.
     * @param timeString String that contains the number and Unit of Time (m, w, d, h, min, sec).
     * @return Milliseconds if successful, otherwise 0;
     */
    public static long getStringMillis(String timeString) {
        long timeMillis;
        try {
            if (timeString.endsWith("m")) {
                timeMillis = TimeMillis.monthMillis(timeString);
            }
            else if (timeString.endsWith("w")) {
                timeMillis = TimeMillis.weekMillis(timeString);
            }
            else if (timeString.endsWith("d")) {
                timeMillis = TimeMillis.dayMillis(timeString);
            }
            else if (timeString.endsWith("h")) {
                timeMillis = TimeMillis.hourMillis(timeString);
            }
            else if (timeString.endsWith("min")) {
                timeMillis = TimeMillis.minuteMillis(timeString);
            }
            else if (timeString.endsWith("sec")) {
                timeMillis = TimeMillis.secondMillis(timeString);
            }
            else{
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return timeMillis;
    }

    /**
     * Calculates the remaining milliseconds until midnight (CET).
     * @return The remaining milliseconds until midnight.
     */
    public static long millisToMidnight() {
        ZoneId z = ZoneId.of("CET");
        ZonedDateTime now = ZonedDateTime.now(z);
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        ZonedDateTime tomorrowStart = tomorrow.atStartOfDay(z);
        return Duration.between(now, tomorrowStart).toMillis();
    }

    /**
     * Converts milliSeconds to Date.
     * @param millis the date in milliseconds.
     * @return Date String in the following format (dd.MM.yyyy HH:mm).
     */
    public static String dateFromMillis(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(new Date(millis));
    }

    /**
     * Converts the String to a Date. Format example (2020-03-27T12:00:00.000Z).
     * @return Date that represents the provided String.
     */
    public static Date stringToDate(String dateString) {
        dateString = dateString.replaceAll("T", "").replaceAll("Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
