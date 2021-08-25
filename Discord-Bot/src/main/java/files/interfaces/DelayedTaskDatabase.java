package main.java.files.interfaces;

import java.util.Date;
import java.util.UUID;

public interface DelayedTaskDatabase {
    void load();
    void addTask(Date date, String taskType, String taskData);

    void deleteTask(long taskId);
    void deleteTask(UUID taskUUID);
}
