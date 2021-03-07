package main.java.files.interfaces;

import lombok.NonNull;

/**
 * Interface used to access the timedtasks table in the database.
 *
 * @author Asklios
 * @version 08.12.2020
 */
public interface TimedTasksDatabase {

    /**
     * Saves a new Task to the database.
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type Type of task. Must be registered at TimeTask.taskFinder
     * @param note An optional note.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    int addTask(@NonNull long endTime, @NonNull String type, String note);
    /**
     * Saves a new Task to the database.
     * @param endTime Time in milliseconds when the Task should be executed.
     * @param type Type of task. Must be registered at TimeTask.taskFinder
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    int addTask(@NonNull long endTime, @NonNull String type);

    /**
     * Gets the note from the first entry of this type.
     * @param type Type of task.
     * @return The note String.
     */
    String getFirstNoteByType(String type);

    /**
     * Removes all entries of the provided type.
     * @param type Type of task.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing | SQLException
     */
    int removeAllEntriesByType(String type);

    /**
     * Gets a new HashMap from the database values.
     */
    void updateAllTasksFromDb();

    void removeTask(long endTime, String type);
}
