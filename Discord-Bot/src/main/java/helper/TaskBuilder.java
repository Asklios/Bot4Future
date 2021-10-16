package main.java.helper;

import com.google.gson.Gson;
import main.java.helper.tasks.UnbanTask;
import main.java.helper.tasks.UnmuteTask;

import java.util.Optional;

public class TaskBuilder {
    public static class TaskData {
        public String guildId;
        public String userId;
        public String reason;
        public String actionDay;
        public long link;

        public static TaskData of(String data) {
            return new Gson().fromJson(data, TaskData.class);
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public static Optional<TaskContext> buildTaskExecutor(String taskType, String taskData) {
        switch (taskType.toUpperCase()) {
            case "UNBAN": {
                TaskData pair = TaskData.of(taskData);
                return Optional.of(jda -> {
                    new UnbanTask().unban(pair);
                });
            }
            case "UNMUTE": {
                TaskData pair = TaskData.of(taskData);
                return Optional.of(jda -> {
                    new UnmuteTask().unmute(pair);
                });
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        TaskData data = new TaskData();
        data.reason = "\"";
        System.out.println(TaskData.of(data.toString()).toString());
    }
}
