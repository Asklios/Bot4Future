package main.java.files.impl;

import main.java.DiscordBot;
import main.java.files.LiteSQL;
import main.java.files.interfaces.DelayedTaskDatabase;
import main.java.helper.TaskBuilder;
import main.java.helper.TaskContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DelayedTaskDatabaseSQLite implements DelayedTaskDatabase {
    private Map<Long, Task> tasks = new HashMap<>();
    private Timer timer = new Timer("DELAYED_TASK_TIMER");

    @Override
    public void load() {
        tasks.clear();
        ResultSet result = LiteSQL.onQuery("SELECT * FROM delayed_tasks;");
        try {
            while (result.next()) {
                long id = result.getLong("id");
                long d = result.getLong("date");
                TaskBuilder.buildTaskExecutor(result.getString("type"), result.getString("data"))
                        .ifPresent(ctx -> {
                            Task t = new Task();
                            t.date = new Date(d);
                            t.task = ctx;
                            tasks.put(id, t);
                        });

            }
        } catch (SQLException e) {
            System.err.println("Error while loading delayed tasks");
            e.printStackTrace();
        }
        tasks.forEach((id, ctx) -> {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ctx.task.run(DiscordBot.INSTANCE.jda);
                    deleteTask(id);
                }
            }, ctx.date);
        });
    }

    @Override
    public void addTask(Date date, String taskType, String taskData) {
        UUID uuid = UUID.randomUUID();
        PreparedStatement stmt = LiteSQL.prepStmt("INSERT INTO delayed_tasks (uuid, date, type, data) VALUES (?, ?, ?, ?)");
        try {
            stmt.setString(1, uuid.toString());
            stmt.setLong(2, date.getTime());
            stmt.setString(3, taskType);
            stmt.setString(4, taskData);

            stmt.execute();
            stmt.close();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TaskBuilder.buildTaskExecutor(taskType, taskData).ifPresent(runner -> {
                        runner.run(DiscordBot.INSTANCE.jda);
                        deleteTask(uuid);
                    });
                }
            }, date);
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void deleteTask(long taskId) {
        LiteSQL.onUpdate("DELETE FROM delayed_tasks WHERE id=" + taskId);
    }

    @Override
    public void deleteTask(UUID taskUUID) {
        LiteSQL.onUpdate("DELETE FROM delayed_tasks WHERE uuid=\"" + taskUUID.toString() + "\"");
    }

    private class Task {
        public Date date;
        public TaskContext task;
    }
}
