package main.java.files;

import main.java.DiscordBot;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * Class for connecting and using the database.
 *
 * @author Asklios
 * @version 18.11.2020
 */

public class LiteSQL {

    private static Connection conn;
    private static Statement stmt;

    /**
     * Connects to the database.
     */
    public static void connect() {
        conn = null;

        try {

            File file = new File(DiscordBot.INSTANCE.getDbFilePath());

            if (!file.exists()) {
                file.createNewFile();
            }

            String url = "jdbc:sqlite:" + file.getPath(); //jdbc = Java Database Connectivity
            conn = DriverManager.getConnection(url);

            System.out.println("Verbindung zur Datenbank hergestellt.");

            stmt = conn.createStatement();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void vacuum() {
        try {
            stmt.execute("VACUUM");
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
            if (conn != null) {
                conn.close();
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
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Requests the database with the provided sql statement.
     * @param sql that should be executed. May result in a SQL injection.
     * @return ResultSet resulting from the database request, null if a SQLException occurs.
     */
    public static ResultSet onQuery(String sql) {
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Provides a PreparedStatement that can be used to prevent a SQL injection.
     * @param sql with ? instead of the values.
     * @return PreparedStatement if successful, null if a SQLException occurs.
     */
    public static PreparedStatement prepStmt(String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Provides a Statement that can be used for batches
     * @return Statement if successful, null if a SQLException occurs.
     */
    public static Statement getStatement(){
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
