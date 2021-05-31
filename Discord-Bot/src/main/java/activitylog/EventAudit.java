package main.java.activitylog;

import lombok.Getter;
import lombok.Setter;
import main.java.files.LiteSQL;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

public class EventAudit {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Getter @Setter
    private static HashMap<Long, List<Long>> ignoredChannels = new HashMap<>();

    public void ignoredChannelsStartUpEntries(Guild guild) {
        long guildId = guild.getIdLong();

        ResultSet result = LiteSQLActivity.onQuery("SELECT * FROM ignoredchannels WHERE guildid = " + guildId);

        try {
            assert result != null;
            if (result.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LiteSQLActivity.onUpdate("INSERT INTO ignoredchannels(guildid) VALUES(" + guildId + ")");
    }

    public void updateIgnoredChannels() {
        PreparedStatement prepStmt = LiteSQLActivity.prepStmt("SELECT * FROM ignoredchannels");
        try {
            ResultSet result = LiteSQLActivity.onQuery("SELECT * FROM ignoredchannels");
            if (result == null) {
                return;
            }
            while (result.next()) {
                long guildId = result.getLong("guildid");
                String channelIds = result.getString("channelids");
                List<Long> ignoredIds = new ArrayList<>();
                if (channelIds != null) {
                    Arrays.stream(channelIds.split(",")).mapToLong(Long::parseLong).forEach(ignoredIds::add);
                }
                ignoredChannels.put(guildId, ignoredIds);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void messageUpdateAudit(MessageUpdateEvent event) {

        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        try {
            if (ignoredChannels.get(guildId).contains(channelId)) { //TODO update message in database
                return;
            }
        } catch (NullPointerException e) {
            //
        }

        String decryptText = new CryptoMessageHandler().readEncryptedMessage(event.getGuild().getIdLong(),
                event.getChannel().getIdLong(), event.getMessageIdLong());
        if (decryptText == null) decryptText = "`unknown message`";
        decryptText = trimZeros(decryptText);

        String newText = event.getMessage().getContentDisplay();
        User user = Objects.requireNonNull(event.getMember()).getUser();

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0x00ccff); //light blue
        b.setTitle(":pencil: Nachricht bearbeitet", event.getMessage().getJumpUrl());
        b.setFooter(event.getMember().getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.addField("Channel", "<#" + event.getChannel().getId() + ">", true);
        b.addField("User", user.getAsMention(), true);
        b.addBlankField(true);
        b.addField("Alte Nachricht", decryptText, true);
        b.addField("Bearbeitete Nachricht", newText, true);
        b.addField("Message ID: ", event.getMessage().getId(), false);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();

        //update encrypted message in database
        long messageId = event.getMessage().getIdLong();
        long userId = user.getIdLong();

        new CryptoMessageHandler().updateMessage(newText, guildId, channelId, messageId, userId);
    }

    public void messageDeleteAudit(MessageDeleteEvent event) {

        if (!event.isFromType(ChannelType.TEXT)) {
            return;
        }

        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        try {
            if (ignoredChannels.get(guildId).contains(channelId)) {
                return;
            }
        } catch (NullPointerException e) {
            //
        }

        String decryptText = new CryptoMessageHandler().readEncryptedMessageWithId(event.getGuild().getIdLong(),
                event.getChannel().getIdLong(), event.getMessageIdLong());
        if (decryptText == null) decryptText = "`unknown message`";
        decryptText = trimZeros(decryptText);

        long userId;
        try {
            userId = Long.parseLong(decryptText.substring(0, 18));
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            //unknown message
            return;
        }

        decryptText = decryptText.substring(18);
        Member member = event.getGuild().getMemberById(userId);
        assert member != null;
        User user = member.getUser();

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0xff9933); //light orange
        b.setTitle(":wastebasket: Nachricht gelöscht");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.addField("Channel", "<#" + event.getChannel().getId() + ">", true);
        b.addField("User", user.getAsMention(), true);
        b.addBlankField(true);
        b.addField("gelöschte Nachricht", decryptText, true);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    public void messageBulkDelete(MessageBulkDeleteEvent event) {
        int count =  event.getMessageIds().size();
        String channel = event.getChannel().getAsMention();

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0xff6600); //orange
        b.setTitle(":wastebasket: Bulk delete");
        b.appendDescription("in " + channel + ", " + count + "gelöschte Nachrichten");

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    private String trimZeros(String str) {
        int pos = str.indexOf(0);
        return pos == -1 ? str : str.substring(0, pos);
    }

    public void messageVoiceJoin(GuildVoiceJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        String channel = "<#" + event.getChannelJoined().getId() + ">";

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0x00ff00); //lime
        b.setTitle(":arrow_forward: Voice join");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.addField("channel", channel, false);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    public void messageVoiceMove(GuildVoiceMoveEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        String last_channel = "<#" + event.getChannelLeft().getId() + ">";
        String channel = "<#" + event.getChannelJoined().getId() + ">";

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0x00ff00); //lime
        b.setTitle(":track_next: Voice move");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.addField("von", last_channel, true);
        b.addField("nach", channel, true);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    public void messageVoiceLeave(GuildVoiceLeaveEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        String last_channel = "<#" + event.getChannelLeft().getId() + ">";

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0x00ff00); //lime
        b.setTitle(":stop_button: Voice leave");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.addField("channel", last_channel, false);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    public void messageRoleAdded(GuildMemberRoleAddEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        Role role = event.getRoles().get(0);
        String roleString = "`" + role.getName() + " (" + role.getId() + ")`";

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0xffff66); //light yellow
        b.setTitle(":roll_of_paper: Role add");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.appendDescription(member.getAsMention() + " -> " + roleString);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }

    public void messageRoleRemoved(GuildMemberRoleRemoveEvent event) {
        Member member = event.getMember();
        User user = member.getUser();
        Role role = event.getRoles().get(0);
        String roleString = "`" + role.getName() + " (" + role.getId() + ")`";

        EmbedBuilder b = new EmbedBuilder();

        b.setTimestamp(OffsetDateTime.now());
        b.setColor(0xffff66); //light yellow
        b.setTitle(":no_pedestrians: Role removed");
        b.setFooter(member.getEffectiveName() + " ("
                + user.getId() + ")", user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
        b.appendDescription(member.getAsMention() + " removed from " + roleString);

        MessageChannel eventAudit = channelDatabase.getEventAuditChannel(event.getGuild());
        if (eventAudit == null) return;
        eventAudit.sendMessage(b.build()).queue();
    }
}
