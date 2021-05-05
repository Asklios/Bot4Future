package main.java.activitylog;

import main.java.files.LiteSQL;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class for creating missing tables in the activity-database on startup.
 *
 * @author Asklios
 * @version 13.04.2021
 */
public class ActivitySQLManager {

    public static boolean onCreate() {
        Statement stmt = LiteSQLActivity.createStatement();
        if(stmt == null) return false;
        try {
            stmt.addBatch("CREATE TABLE IF NOT EXISTS messages(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "messageid INTEGER, encrypted BLOB)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS ignoredchannels(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "guildid INTEGER, channelids INTEGER)");
            stmt.executeBatch();

            Connection connection = stmt.getConnection();
            stmt.close();
            connection.close();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }

    }
}
