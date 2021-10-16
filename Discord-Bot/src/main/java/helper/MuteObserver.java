package main.java.helper;

import main.java.DiscordBot;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.tasks.UnmuteTask;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Event reactions connected to the mute task.
 *
 * @author Asklios
 * @version 01.01.2021
 */
public class MuteObserver {

    /**
     * When a new member joins, a check is made to see if they have been muted.
     * If so, the mute role is added.
     */
    public static void guildMemberJoin(GuildMemberJoinEvent event) {
        UserRecordsDatabaseSQLite userRecordsDatabase = new UserRecordsDatabaseSQLite();
        RoleDatabase roleDatabase = new RoleDatabaseSQLite();
        Guild guild = event.getGuild();
        long userId = event.getUser().getIdLong();

        if (userRecordsDatabase.wasMuted(guild.getIdLong(), userId)) {
            Role muteRole = roleDatabase.getMuteRole(event.getGuild());
            guild.addRoleToMember(event.getMember(), muteRole).queue();
        }
    }

    /**
     * When a member is updated, a check is made to see if they were muted and the mute role was removed.
     * If true, the user will be unmuted.
     */
    public static void onGuildMemberUpdate(GuildMemberUpdateEvent event) {
        RoleDatabase roleDatabase = new RoleDatabaseSQLite();
        UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();
        Guild guild = event.getGuild();
        long userId = event.getUser().getIdLong();

        List<Long> recordIds = userRecordsDatabase.unliftedMutesByUserIdAndGuildId(userId, guild.getIdLong());

        if (recordIds.isEmpty()) return;

        Role muterole = roleDatabase.getMuteRole(event.getGuild());
        if (guild.retrieveMemberById(userId).complete().getRoles().contains(muterole)) {
            return;
        }

        Map<Long, TaskBuilder.TaskData> taskData = new HashMap<>();

        ResultSet tasks = DiscordBot.INSTANCE.delayedTasks.getTasksByType("UNMUTE");
        try {
            while (tasks.next()) {
                taskData.put(tasks.getLong("id"), TaskBuilder.TaskData.of(tasks.getString("data")));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        AtomicBoolean reload = new AtomicBoolean(false);
        taskData.forEach((id, data) -> {
            if(data.guildId.equals(guild.getId()) && data.userId.equals(userId)){
                reload.set(true);
                new UnmuteTask().unmute(data);
                DiscordBot.INSTANCE.delayedTasks.deleteTask(id);
            }
        });

        if(reload.get())
            DiscordBot.INSTANCE.delayedTasks.reload();
    }
}