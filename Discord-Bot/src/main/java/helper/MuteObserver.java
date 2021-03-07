package main.java.helper;

import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Event reactions connected to the mute task.
 * @author Asklios
 * @version 01.01.2021
 */
public class MuteObserver {

    /**
     * When a new member joins, a check is made to see if they have been muted.
     * If so, the mute role is added.
     */
    public static void guildMemberJoin(GuildMemberJoinEvent event) {
        RoleDatabase roleDatabase = new RoleDatabaseSQLite();
        Guild guild = event.getGuild();
        long userId = event.getUser().getIdLong();
        List<UserRecord> userRecords = new UserRecords().userRecordByTypeGuildUser(guild.getIdLong(),
                UserRecord.RecordType.MUTE, userId);
        if (userRecords.isEmpty()) return;

        boolean[] wasMuted = new boolean[1];

        userRecords.forEach(u -> {
            if (u.getNote().equals("liftet")) return;
            wasMuted[0] = true;
        });

        if (wasMuted[0]) {
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
        Guild guild = event.getGuild();
        long userId = event.getUser().getIdLong();

        List<UserRecord> userRecords = new UserRecords().userRecordByTypeGuildUser(guild.getIdLong(),
                UserRecord.RecordType.MUTE, userId);
        if (userRecords.isEmpty()) return;

        Role muterole = roleDatabase.getMuteRole(event.getGuild());
        if (guild.retrieveMemberById(userId).complete().getRoles().contains(muterole)) {
            return;
        }

        List<TimedTask> timedTasks = new ArrayList<>();

        userRecords.forEach(u -> {
            if (u.getNote().equals("liftet")) return;

            try {
                TimedTasks.tasks.forEach((aLong, timedTask) -> {
                    if (timedTask.getType() != TimedTask.TimedTaskType.UNMUTE) return;
                    try {
                        if (!(timedTask.getNote().equals(u.getId() + ""))) return;
                        timedTasks.add(timedTask);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
        });
        for (TimedTask timedTask : timedTasks) {
            new UnMute().liftMute(timedTask);
            new TimedTasks().removeTimedTask(timedTask);
        }
    }
}