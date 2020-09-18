package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelDatabaseSQLite implements ChannelDatabase {

    @Override
    public void startUpEntries(Guild guild) throws NullPointerException {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT * FROM guildchannels WHERE guildid = " + guildid);

        try {
            if (result.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LiteSQL.onUpdate("INSERT INTO guildchannels(guildid, type) VALUES(" + guildid + ", 'audit')");
        LiteSQL.onUpdate("INSERT INTO guildchannels(guildid, type) VALUES(" + guildid + ", 'pnchannel')");
    }

    @Override
    public void saveAuditChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " +  guildId + " AND type = 'audit'");
    }

    @Override
    public TextChannel getAuditChannel(Guild guild) throws NullPointerException {
        long guildId = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT channelid FROM guildchannels WHERE guildid = " + guildId + " AND type = 'audit'");

        try {
            if (result.next()) {
                long channelId = result.getLong("channelid");
                return guild.getTextChannelById(channelId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void savePnChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " +  guildId + " AND type = 'pnchannel'");
    }

    @Override
    public TextChannel getPnChannel(Guild guild) throws NullPointerException{
        long guildId = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT channelid FROM guildchannels WHERE guildid = " + guildId + " AND type = 'pnchannel'");

        try {
            if (result.next()) {
                long channelId = result.getLong("channelid");
                return guild.getTextChannelById(channelId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
