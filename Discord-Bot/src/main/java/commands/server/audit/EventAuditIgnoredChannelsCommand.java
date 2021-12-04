package main.java.commands.server.audit;

import main.java.activitylog.EventAudit;
import main.java.activitylog.LiteSQLActivity;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EventAuditIgnoredChannelsCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        long guildId = message.getGuild().getIdLong();
        String channelIds = message.getMentionedChannels().stream().map(ISnowflake::getId)
                .collect(Collectors.joining(","));

        PreparedStatement prepStmt = LiteSQLActivity
                .prepStmt("INSERT INTO ignoredchannels(guildid, channelids) VALUES(?,?)");

        try {
            assert prepStmt != null;
            prepStmt.setLong(1, guildId);
            prepStmt.setString(2, channelIds);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HashMap<Long, List<Long>> ignoredChannels = EventAudit.getIgnoredChannels();
        List<Long> ignoredIds = new ArrayList<>();
        Arrays.stream(channelIds.split(",")).mapToLong(Long::parseLong).forEach(ignoredIds::add);
        ignoredChannels.replace(guildId, ignoredIds);
        EventAudit.setIgnoredChannels(ignoredChannels);

        channel.sendMessage("ignoring " + message.getMentionedChannels().stream()
                .map(IMentionable::getAsMention).collect(Collectors.joining(", "))).queue();
    }
}
