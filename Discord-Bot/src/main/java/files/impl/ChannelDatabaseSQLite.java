package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class for requests to the guildchannels table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public class ChannelDatabaseSQLite implements ChannelDatabase {

    /**
     * Adds empty entries for every guild. Enables SQL: UPDATE
     * @param guild the id of the new guild.
     */
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

    /**
     * Saves the TextChannel for audit-messages.
     * @param textChannel the specified audit-channel.
     */
    @Override
    public void saveAuditChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " +  guildId + " AND type = 'audit'");
    }

    /**
     * Returns the saved TextChannel for audit-messages.
     * @param guild the requesting guild.
     * @return The audit TextChannel or null if it is not yet defined.
     */
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

    /**
     * Saves the TextChannel for Messages from the pmSystem.
     * @param textChannel the specified pm-channel.
     */
    @Override
    public void savePmChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " +  guildId + " AND type = 'pnchannel'");
    }

    /**
     * Returns the saved TextChannel for the pmSystem.
     * @param guild the requesting guild.
     * @return The pm TextChannel or null if it is not yet defined.
     */
    @Override
    public TextChannel getPmChannel(Guild guild) throws NullPointerException{
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

    /**
     * Sets all entries back to null for the provided guild.
     * @param guildId the id of the requesting guild.
     */
    @Override
    public void removeGuildData(long guildId) {
        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = NULL WHERE guildid = " + guildId);
    }
}
