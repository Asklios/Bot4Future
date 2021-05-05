package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class for requests to the guildchannels table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public class ChannelDatabaseSQLite implements ChannelDatabase {

    /**
     * Adds empty entries for every guild. Enables SQL: UPDATE
     *
     * @param guild the id of the new guild.
     */
    @Override
    public void startUpEntries(Guild guild) throws NullPointerException {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement statement = connection.createStatement();
            long guildid = guild.getIdLong();

            ResultSet result = statement.executeQuery("SELECT type FROM guildchannels WHERE guildid = " + guildid);

            List<String> types = new ArrayList<>();
            try {
                assert result != null;
                while (result.next()) {
                    types.add(result.getString("type"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            result.close();
            statement.close();
            connection.close();
            if (!types.contains("audit"))
                LiteSQL.onUpdate("INSERT INTO guildchannels(guildid, type) VALUES(" + guildid + ", 'audit')");
            if (!types.contains("eventaudit"))
                LiteSQL.onUpdate("INSERT INTO guildchannels(guildid, type) VALUES(" + guildid + ", 'eventaudit')");
            if (!types.contains("pnchannel"))
                LiteSQL.onUpdate("INSERT INTO guildchannels(guildid, type) VALUES(" + guildid + ", 'pnchannel')");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the TextChannel for audit-messages.
     *
     * @param textChannel the specified audit-channel.
     */
    @Override
    public void saveAuditChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " + guildId + " AND type = 'audit'");
    }

    /**
     * Returns the saved TextChannel for audit-messages.
     *
     * @param guild the requesting guild.
     * @return The audit TextChannel or null if it is not yet defined.
     */
    @Override
    public TextChannel getAuditChannel(Guild guild) throws NullPointerException {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();
            long guildId = guild.getIdLong();

            ResultSet result = stmt.executeQuery("SELECT channelid FROM guildchannels WHERE guildid = " + guildId + " AND type = 'audit'");

            if (result.next()) {
                long channelId = result.getLong("channelid");
                result.close();
                stmt.close();
                connection.close();
                return guild.getTextChannelById(channelId);
            }
            long channelId = result.getLong("channelid");
            result.close();
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves the TextChannel for event-audit-messages.
     *
     * @param textChannel the specified event-audit-channel.
     */
    @Override
    public void saveEventAuditChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " + guildId + " AND type = 'eventaudit'");
    }

    /**
     * Returns the saved TextChannel for event-audit-messages.
     *
     * @param guild the requesting guild.
     * @return The event-audit TextChannel or null if it is not yet defined.
     */
    @Override
    public TextChannel getEventAuditChannel(Guild guild) throws NullPointerException {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();
            long guildId = guild.getIdLong();

            ResultSet result = stmt.executeQuery("SELECT channelid FROM guildchannels WHERE guildid = " + guildId + " AND type = 'eventaudit'");

            try {
                if (result.next()) {
                    long channelId = result.getLong("channelid");
                    result.close();
                    stmt.close();
                    connection.close();
                    return guild.getTextChannelById(channelId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            result.close();
            stmt.close();
            connection.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves the TextChannel for Messages from the pmSystem.
     *
     * @param textChannel the specified pm-channel.
     */
    @Override
    public void savePmChannel(TextChannel textChannel) {
        long channelId = textChannel.getIdLong();
        long guildId = textChannel.getGuild().getIdLong();

        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = " + channelId + " WHERE guildid = " + guildId + " AND type = 'pnchannel'");
    }

    /**
     * Returns the saved TextChannel for the pmSystem.
     *
     * @param guild the requesting guild.
     * @return The pm TextChannel or null if it is not yet defined.
     */
    @Override
    public TextChannel getPmChannel(Guild guild) throws NullPointerException {
        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();

            long guildId = guild.getIdLong();

            ResultSet result = stmt.executeQuery("SELECT channelid FROM guildchannels WHERE guildid = " + guildId + " AND type = 'pnchannel'");

            try {
                if (result.next()) {
                    long channelId = result.getLong("channelid");
                    result.close();
                    stmt.close();
                    connection.close();
                    return guild.getTextChannelById(channelId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            result.close();
            stmt.close();
            connection.close();
            return null;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets all entries back to null for the provided guild.
     *
     * @param guildId the id of the requesting guild.
     */
    @Override
    public void removeGuildData(long guildId) {
        LiteSQL.onUpdate("UPDATE guildchannels SET channelid = NULL WHERE guildid = " + guildId);
    }
}
