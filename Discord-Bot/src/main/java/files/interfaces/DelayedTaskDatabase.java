package main.java.files.interfaces;

import java.sql.ResultSet;
import java.util.Date;
import java.util.UUID;

public interface DelayedTaskDatabase {
    void load();
    void reload();
    void addTask(Date date, String taskType, String taskData);

    ResultSet getTasksByType(String taskType);

    void deleteTask(long taskId);
    void deleteTask(UUID taskUUID);
}
