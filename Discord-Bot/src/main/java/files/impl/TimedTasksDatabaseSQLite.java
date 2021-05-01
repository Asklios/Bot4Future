package main.java.files.impl;

import lombok.NonNull;
import main.java.files.LiteSQL;
import main.java.files.interfaces.TimedTasksDatabase;
import main.java.helper.TimedTask;
import main.java.helper.TimedTasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class used to access the timedtasks table in the database.
 *
 * @author Asklios
 * @version 08.12.2020
 */
public class TimedTasksDatabaseSQLite implements TimedTasksDatabase {

    /**
     * Saves a new Task to the database.
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type Type of task. Must be registered at TimeTask.taskFinder
     * @param note An optional note.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int addTask(@NonNull long endTime, @NonNull String type, String note) {

        PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO timedtasks(endtime, type, note) VALUES(?,?,?)");
        try {
            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            prepStmt.setString(3, note);
            int result = prepStmt.executeUpdate();
            LiteSQL.closePreparedStatement(prepStmt);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Saves a new Task to the database.
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type Type of task. Must be registered at TimeTask.taskFinder
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int addTask(@NonNull long endTime, @NonNull String type) {

        PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO timedtasks(endtime, type) VALUES(?,?)");
        try {
            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            int res = prepStmt.executeUpdate();
            LiteSQL.closePreparedStatement(prepStmt);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the note from the first entry of this type.
     * @param type Type of task.
     * @return The note String.
     */
    @Override
    public String getFirstNoteByType(String type) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT note FROM timedtasks WHERE type = ?");
        try {
            prepStmt.setString(1, type);
            ResultSet result = prepStmt.executeQuery();
            if (result.next()) {
                return result.getString("note");
            }
            return "";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes all entries of the provided type.
     * @param type Type of task.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    @Override
    public int removeAllEntriesByType(String type) {

        TimedTasks.tasks.values().removeIf(s -> s.equals(type));

        PreparedStatement prepStmt = LiteSQL.prepStmt("DELETE FROM timedtasks WHERE type = ?");
        try {
            prepStmt.setString(1, type);
            int res = prepStmt.executeUpdate();
            LiteSQL.closePreparedStatement(prepStmt);
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

        ResultSet result = LiteSQL.onQuery("SELECT * FROM timedtasks");
        if (result == null) return;

        new TimedTasks().clearTimedTasks();

        try {
            while (result.next()) {
                long endTime = result.getLong("endtime");
                String typeString = result.getString("type").toUpperCase();
                String note = result.getString("note");

                TimedTask.TimedTaskType type = new TimedTasks().getTimedTaskTypeFromString(typeString);
                if (note == null) new TimedTasks().addTimedTasktoHashMap(type, endTime);
                else new TimedTasks().addTimedTasktoHashMap(type, endTime, note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a TimedTask from the database
     * @param endTime of the timedTask.
     * @param type of the timedTask.
     */
    @Override
    public void removeTask(long endTime, String type) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("DELETE FROM timedtasks WHERE endtime = ? AND type = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, endTime);
            prepStmt.setString(2, type);
            prepStmt.executeUpdate();
            LiteSQL.closePreparedStatement(prepStmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
