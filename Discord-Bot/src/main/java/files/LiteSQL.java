package main.java.files;

import main.java.DiscordBot;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for connecting and using the database.
 *
 * @author Asklios
 * @version 18.11.2020
 */

public class LiteSQL {

    public static BasicDataSource POOL;
    private static final Map<ResultSet, Statement> statements = new HashMap<>();

    /**
     * Connects to the database.
     */
    public static void connect() {
        POOL = null;
        try {

            File file = new File(DiscordBot.INSTANCE.getDbFilePath());

            if (!file.exists()) {
                file.createNewFile();
            }

            String url = "jdbc:sqlite:" + file.getPath(); //jdbc = Java Database Connectivity
            //conn = DriverManager.getConnection(url);

            POOL = new BasicDataSource();
            POOL.setUrl(url);
            System.out.println("Verbindung zur Datenbank hergestellt.");

            //stmt = conn.createStatement();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void vacuum() {
        try {
            Connection con = POOL.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute("VACUUM");
            stmt.close();
            con.close();
            System.out.println("Database VACUUM");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects from the database.
     */
    public static void disconnect() {
        try {
            if (POOL != null) {
                POOL.close();
                System.out.println("Verbindung zur Datenbank getrennt.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the database with the provided sql statement.
     * @param sql that should be executed. May result in a SQL injection.
     * @return true if successful, otherwise false.
     */
    public static boolean onUpdate(String sql) {

        try {
            Connection con = POOL.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute(sql);
            stmt.close();
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
