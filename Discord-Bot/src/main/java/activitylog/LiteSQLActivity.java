package main.java.activitylog;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;

/**
 * Class for connecting and using the activity-database.
 *
 * @author Asklios
 * @version 18.11.2020
 */
public class LiteSQLActivity {

    private static BasicDataSource POOL;

    /**
     * Connects to the database.
     */
    public static void connect() {
        POOL = null;

        try {

            File file = new File("data/activity.db");

            if (!file.exists()) {
                file.createNewFile();
            }

            String url = "jdbc:sqlite:" + file.getPath(); //jdbc = Java Database Connectivity
            POOL = new BasicDataSource();
            POOL.setUrl(url);

            System.out.println("Verbindung zur Datenbank hergestellt.");
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
            Connection connection = POOL.getConnection();
            Statement stmt = connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            connection.close();
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
            Connection connection = POOL.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            stmt.close();
            connection.close();
            return resultSet;
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
            Connection connection = POOL.getConnection();
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
