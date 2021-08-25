package main.java.helper;

import com.google.gson.Gson;
import main.java.helper.tasks.UnbanTask;
import main.java.helper.tasks.UnmuteTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import javax.swing.text.html.Option;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;

public class TaskBuilder {
    public static class GuildUserPair {
        public String guildId;
        public String userId;
        public String reason;
        public String actionDay;

        public static GuildUserPair of(String data) {
            return new Gson().fromJson(data, GuildUserPair.class);
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public static Optional<TaskContext> buildTaskExecutor(String taskType, String taskData) {
        switch (taskType.toUpperCase()) {
            case "UNBAN": {
                GuildUserPair pair = GuildUserPair.of(taskData);
                return Optional.of(jda -> {
                    new UnbanTask().unban(pair);
                });
            }
            case "UNMUTE": {
                GuildUserPair pair = GuildUserPair.of(taskData);
                return Optional.of(jda -> {
                    new UnmuteTask().unmute(pair);
                });
            }
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        GuildUserPair data = new GuildUserPair();
        data.reason = "\"";
        System.out.println(GuildUserPair.of(data.toString()).toString());
    }
}
