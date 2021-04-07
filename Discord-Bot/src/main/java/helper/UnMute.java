package main.java.helper;

import main.java.DiscordBot;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.RoleDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class UnMute {

    RoleDatabase roleDatabase = new RoleDatabaseSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    public void liftMute(TimedTask timedTask) {
        long userRecordId = Long.parseLong(timedTask.getNote());
        UserRecord userRecord = userRecordsDatabase.recordById(userRecordId);

        //if there is no userRecord with the provided id
        if (userRecord == null) return;

        long guildId = userRecord.getGuildId();
        long userId = userRecord.getUserId();
        String[] roleIds = userRecord.getNote().split("⫠");

        JDA jda = DiscordBot.INSTANCE.jda;
        Guild guild = jda.getGuildById(guildId);
        assert guild != null;
        Member[] member = new Member[1];
        try {
            member[0] = guild.retrieveMemberById(userId).complete();
        } catch (ErrorResponseException e) {
            //unknown member
            return;
        }

        if (member[0] == null) return;

        Role muteRole = roleDatabase.getMuteRole(guild);

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0x1DA64A);
        b.setThumbnail(member[0].getUser().getEffectiveAvatarUrl());
        b.addField("Name: ", member[0].getAsMention(), true);
        b.addField("ID: ", userId + "", true);
        b.addField(":alarm_clock: gemutet seit: ", TimeMillis.dateFromMillis(userRecord.getDate()) + "", false);
        b.addField(":page_facing_up: Mute Begründung: ", userRecord.getReason(), false);
        b.setTimestamp(OffsetDateTime.now());

        if (member[0].getRoles().contains(muteRole)) {
            guild.removeRoleFromMember(member[0], muteRole).queue();

            b.setTitle(":speaker: Nutzer*in entmutet");
            b.setFooter("by " + guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getUser().getEffectiveAvatarUrl());
        }
        else {
            b.setTitle(":speaker: Nutzer*in manuell entmutet");
            b.setFooter("noticed by " + guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getUser().getEffectiveAvatarUrl());
        }

        List<Role> memberRoles = new ArrayList<>();

        for (String s : roleIds) {
            memberRoles.add(guild.getRoleById(Long.parseLong(s)));
        }

        memberRoles.forEach(r -> guild.addRoleToMember(member[0], r).queue());

        TextChannel audit = channelDatabase.getAuditChannel(guild);
        if (audit == null) return;

        audit.sendMessage(b.build()).queue();

        userRecordsDatabase.setNoteLiftedById(userRecordId);
    }
}
