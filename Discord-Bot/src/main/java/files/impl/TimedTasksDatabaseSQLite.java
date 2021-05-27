package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.TimedTasksDatabase;
import main.java.helper.TimedTask;
import main.java.helper.TimedTasks;

import java.sql.*;

/**
 * Class used to access the timedtasks table in the database.
 *
 * @author Asklios
 * @version 08.12.2020
 */
public class TimedTasksDatabaseSQLite implements TimedTasksDatabase {

    /**
     * Saves a new Task to the database.
     *
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type    Type of task. Must be registered at TimeTask.taskFinder
     * @param note    An optional note.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int addTask(long endTime, String type, String note) {

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO timedtasks(endtime, type, note) VALUES(?,?,?)");

            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            prepStmt.setString(3, note);
            int result = prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Saves a new Task to the database.
     *
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type    Type of task. Must be registered at TimeTask.taskFinder
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int addTask(long endTime, String type) {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO timedtasks(endtime, type) VALUES(?,?)");

            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            int res = prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the note from the first entry of this type.
     *
     * @param type Type of task.
     * @return The note String.
     */
    @Override
    public String getFirstNoteByType(String type) {
        try {
            Connection connection = LiteSQL.POOL.getConnection();

            PreparedStatement prepStmt = connection.prepareStatement("SELECT note FROM timedtasks WHERE type = ?");

            prepStmt.setString(1, type);
            ResultSet result = prepStmt.executeQuery();
            if (result.next()) {
                result.close();
                prepStmt.close();
                connection.close();
                return result.getString("note");
            }
            result.close();
            prepStmt.close();
            connection.close();
            return "";

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes all entries of the provided type.
     *
     * @param type Type of task.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int removeAllEntriesByType(String type) {

        TimedTasks.tasks.values().removeIf(s -> s.equals(type));

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("DELETE FROM timedtasks WHERE type = ?");

            prepStmt.setString(1, type);
            int res = prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
            return res;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Gets a new HashMap from the database values.
     */
    @Override
    public void updateAllTasksFromDb() {

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM timedtasks");

            new TimedTasks().clearTimedTasks();
            while (result.next()) {
                long endTime = result.getLong("endtime");
                String typeString = result.getString("type").toUpperCase();
                String note = result.getString("note");

                TimedTask.TimedTaskType type = new TimedTasks().getTimedTaskTypeFromString(typeString);
                if (note == null) new TimedTasks().addTimedTasktoHashMap(type, endTime);
                else new TimedTasks().addTimedTasktoHashMap(type, endTime, note);
            }
            result.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a TimedTask from the database
     *
     * @param endTime of the timedTask.
     * @param type    of the timedTask.
     */
    @Override
    public void removeTask(long endTime, String type) {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("DELETE FROM timedtasks WHERE endtime = ? AND type = ?");

            assert prepStmt != null;
            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
