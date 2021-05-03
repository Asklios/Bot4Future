package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.CallDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Class for requesting the calldata table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */

public class CallDatabaseSQLite implements CallDatabase {

    /**
     * Checks if the call-name is already taken.
     * @param guildId the id of the current guild.
     * @param name the user provided call-name.
     * @return returns true if the name does not exist yet, otherwise false.
     */
    @Override
    public boolean checkName(long guildId, String name) {
        boolean free = false;
        try {
            PreparedStatement pstmt = LiteSQL.prepStmt("SELECT * FROM calldata WHERE guildid = ? AND name = ?");
            pstmt.setLong(1, guildId);
            pstmt.setString(2, name);

            ResultSet result = pstmt.executeQuery();

            if (!result.next()) {
                free = true;
            }
            LiteSQL.closePreparedStatement(pstmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return free;
    }

    /**
     * Saves the collected presence data to the database an returns the entry id.
     * @param guild the id of the current guild.
     * @param users a String of the participating userIds.
     * @param startTime the startTime of the call in milliseconds.
     * @param endTime the endTime of the call in milliseconds.
     * @param name the user provided call-name.
     * @param requester the userId from the command user.
     * @return returns the entry id from the database, 0 if there has been an exception.
     */
    @Override
    public long saveCallData(Guild guild, String users, long startTime, long endTime, String name, long requester) {
        try {
            PreparedStatement pstmt = LiteSQL.prepStmt("INSERT INTO calldata(guildid, userids, starttime, endtime, name, requester) VALUES(?,?,?,?,?,?)");
            pstmt.setLong(1, guild.getIdLong());
            pstmt.setString(2, users);
            pstmt.setLong(3, startTime);
            pstmt.setLong(4, endTime);
            pstmt.setString(5, name);
            pstmt.setLong(6, requester);
            pstmt.executeUpdate();
            LiteSQL.closePreparedStatement(pstmt);
            ResultSet result = LiteSQL.onQuery("SELECT id FROM calldata WHERE guildid = " + guild.getIdLong() + " AND starttime = " + startTime);
            return result.getLong("id");
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Can search the database for an entry by id or call-name.
     * Checks if the user has the permission ADMINISTRATOR or has requested the data collection himself.
     * @param search can be an entry id or the call-name.
     * @param channel the textChannel the request comes from.
     * @param member the member that is requesting the information.
     * @return the String of userIds that was previously saved, empty if there is no entry, null for missing permissions.
     */
    @Override
    public String getUsersFromDb(String search, TextChannel channel, Member member) {

        String users = "";
        ResultSet result = null;
        boolean isId;

        try {
            Long.parseLong(search);
            //search contains an id
            isId = true;

            result = LiteSQL.onQuery("SELECT userids, requester FROM calldata WHERE id = " + search + " AND guildid = " + channel.getGuild().getId());
        }
        catch (NumberFormatException e) {
            //search contains a name
            isId = false;

            try {
                PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT userids, requester FROM calldata WHERE name = ? AND guildid = ?");
                prepStmt.setString(1, search);
                prepStmt.setLong(2, channel.getGuild().getIdLong());
                result = prepStmt.executeQuery();
                LiteSQL.closePreparedStatement(prepStmt);


            } catch (SQLException f) {
                e.printStackTrace();
            }
        }

        //checks if the requester id matches or if the user has the admin permission
        try {
            if (result.next()) {

                long requesterId = Long.parseLong(result.getString("requester"));

                if (member.getIdLong() != requesterId) {
                    if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
                        channel.sendMessage("Du besitzt nicht die nötigen Rechte. Daten können nur von der Commandnutzer*in oder einem Admin abgerufen werden.")
                                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                        return null;
                    }
                    else {
                        users = result.getString("userids");
                    }
                }
                else {
                    users = result.getString("userids");
                }

            } else {
                if (isId) {
                    channel.sendMessage("Die eingegebene Id konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                }
                else {
                    channel.sendMessage("Der eingegebene Name konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Deletes all entries that are related to the provided guild.
     * @param guildId the id of the requesting guild.
     * @return true if deleted successful, false if there were no entries.
     */
    @Override
    public boolean removeGuildData(long guildId) {

        ArrayList<Integer> ids = new ArrayList<>();

        ResultSet result = LiteSQL.onQuery("SELECT id FROM calldata WHERE guildid = " + guildId);

        try {
            while (result.next()) {
                ids.add(result.getInt("id"));
            }
        } catch (NullPointerException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (ids.isEmpty()) return false;

        for (long i : ids) LiteSQL.onUpdate("DELETE FROM calldata WHERE id = " + i);

        return true;
    }
}
