package main.java.activitylog;

/**
 * Class for creating missing tables in the activity-database on startup.
 *
 * @author Asklios
 * @version 13.04.2021
 */
public class ActivitySQLManager {

    public static void onCreate() {
        LiteSQLActivity.onUpdate("CREATE TABLE IF NOT EXISTS messages(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "messageid INTEGER, encrypted BLOB)");
        LiteSQLActivity.onUpdate("CREATE TABLE IF NOT EXISTS ignoredchannels(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "guildid INTEGER, channelids INTEGER)");
    }
}
