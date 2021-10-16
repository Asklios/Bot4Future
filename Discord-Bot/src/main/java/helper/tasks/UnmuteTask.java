package main.java.helper.tasks;

import main.java.DiscordBot;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.RoleDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.TaskBuilder;
import main.java.helper.UserRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;


public class UnmuteTask {

    RoleDatabase roleDatabase = new RoleDatabaseSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    public void unmute(TaskBuilder.TaskData data) {
        userRecordsDatabase.setNoteLiftedById(data.link);
        String guildId = data.guildId;
        String userId = data.userId;

        JDA jda = DiscordBot.INSTANCE.jda;
        Guild guild = jda.getGuildById(guildId);
        assert guild != null;
        try {
            Member member = guild.retrieveMemberById(userId).complete();
            if (member == null) return;

            Role muteRole = roleDatabase.getMuteRole(guild);

            EmbedBuilder b = new EmbedBuilder();
            b.setColor(0x1DA64A);
            b.setThumbnail(member.getUser().getEffectiveAvatarUrl());
            b.addField("Name: ", member.getAsMention(), true);
            b.addField("ID: ", userId + "", true);
            b.addField(":alarm_clock: gemutet seit: ", "<t:" + TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(data.actionDay)) + ">", false);
            if (data.reason != null)
                b.addField(":page_facing_up: Mute Begründung: ", data.reason, false);
            b.setTimestamp(OffsetDateTime.now());

            if (member.getRoles().contains(muteRole)) {
                guild.removeRoleFromMember(member, muteRole).queue();

                b.setTitle(":speaker: Nutzer:in entmutet");
                b.setFooter("by " + guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getUser().getEffectiveAvatarUrl());
            } else {
                b.setTitle(":speaker: Nutzer:in manuell entmutet");
                b.setFooter("noticed by " + guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getUser().getEffectiveAvatarUrl());
            }

            TextChannel audit = channelDatabase.getAuditChannel(guild);
            if (audit == null) return;

            audit.sendMessage(b.build()).queue();
        } catch (ErrorResponseException e) {
            return;
        }
    }
}