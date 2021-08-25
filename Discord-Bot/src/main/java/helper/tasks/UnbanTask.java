package main.java.helper.tasks;

import main.java.DiscordBot;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.RoleDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.TaskBuilder;
import main.java.helper.TimeMillis;
import main.java.helper.TimedTask;
import main.java.helper.UserRecord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;

public class UnbanTask {
    RoleDatabase roleDatabase = new RoleDatabaseSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    public void unban(TaskBuilder.GuildUserPair data) {

        String guildId = data.guildId;
        String userId = data.userId;

        Guild guild = DiscordBot.INSTANCE.jda.getGuildById(guildId);

        assert guild != null;
        guild.unban(userId).queue();

        TextChannel audit = channelDatabase.getAuditChannel(guild);
        if (audit == null) return;

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0x1DA64A);
        b.setTitle("TempBan aufgehoben");
        //b.setThumbnail(member.getUser().getEffectiveAvatarUrl());
        b.addField("Name: ", "<@" + userId + ">", true);
        b.addField("ID: ", userId + "", true);
        b.addField(":alarm_clock: gebannt seit: ", data.actionDay, false);
        b.addField(":page_facing_up: Ban Begründung: ", data.reason, false);
        b.setFooter("by " + guild.getSelfMember().getEffectiveName(), guild.getSelfMember().getUser().getEffectiveAvatarUrl());
        b.setTimestamp(OffsetDateTime.now());

        audit.sendMessage(b.build()).queue();
    }
}
