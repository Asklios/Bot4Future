package main.java.files;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CallDatabase {

    public static boolean checkName(long guildId, String name) {
        boolean free = false;
        try {
            PreparedStatement pstmt = LiteSQL.prepStmt("SELECT * FROM calldata WHERE guildid = ? AND name = ?");
            pstmt.setLong(1, guildId);
            pstmt.setString(2, name);

            ResultSet result = pstmt.executeQuery();

            if (!result.next()) {
                free = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return free;
    }

    public static long saveCallData(Guild guild, String users, long startTime, long endTime, String name, long requester) {

        try {
            PreparedStatement pstmt = LiteSQL.prepStmt("INSERT INTO calldata(guildid, userids, starttime, endtime, name, requester) VALUES(?,?,?,?,?,?)");
            pstmt.setLong(1, guild.getIdLong());
            pstmt.setString(2, users);
            pstmt.setLong(3, startTime);
            pstmt.setLong(4, endTime);
            pstmt.setString(5, name);
            pstmt.setLong(6, requester);
            pstmt.executeUpdate();

            ResultSet result = LiteSQL.onQuery("SELECT id FROM calldata WHERE guildid = " + guild.getIdLong() + " AND starttime = " + startTime);
            return result.getLong("id");
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getUsersFromDb(String search, TextChannel channel, Member member) {

        String users = "";
        ResultSet result = null;
        boolean isId;

        try {
            Long.parseLong(search);
            //es wurde nach einer id gesucht
            isId = true;

            result = LiteSQL.onQuery("SELECT userids, requester FROM calldata WHERE id = " + search + " AND guildid = " + channel.getGuild().getId());
        }
        catch (NumberFormatException e) {
            //es wurde nach dem namen gesucht
            isId = false;

            try {
                PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT userids, requester FROM calldata WHERE name = ? AND guildid = ?");
                prepStmt.setString(1, search);
                prepStmt.setLong(2, channel.getGuild().getIdLong());
                result = prepStmt.executeQuery();

            } catch (SQLException f) {
                e.printStackTrace();
            }
        }

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

    public static boolean removeGuildData(long guildId) {

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
