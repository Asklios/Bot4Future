package main.java.files;

import main.java.DiscordBot;
import main.java.files.impl.TimedTasksDatabaseSQLite;
import main.java.helper.TimeMillis;
import main.java.helper.TimedTask;
import main.java.helper.TimedTasks;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiteSqlClear {

    List<Integer> lineIDs = new ArrayList<Integer>();
    /**
     * 0 -> success
     * 1 -> missing channel access
     */
    List<Byte> returnCodes = new ArrayList<>();

    /**
     * Schedules a database cleaning at midnight.
     */
    public static void timedClearDatabase() {
        new TimedTasks().addTimedTask(TimedTask.TimedTaskType.DBCLEAR, System.currentTimeMillis() + TimeMillis.millisToMidnight());
    }

    /**
     * 0 -> success
     * 1 -> missing channel access
     */
    public List<Byte> clearDatabase() {
        findIdsReactroles();
        deleteLinesById("reactroles");
        lineIDs.clear();
        System.out.println("cleared: reactroles");

        findIdsVotereactions();
        deleteLinesById("votereactions");
        lineIDs.clear();
        System.out.println("cleared: votereactions");

        new TimedTasks().removeOldTasks();
        System.out.println("cleared: timedtasks");

        LiteSQL.vacuum();

        new TimedTasksDatabaseSQLite().removeAllEntriesByType("DBCLEAR");
        new TimedTasks().addTimedTask(TimedTask.TimedTaskType.DBCLEAR, System.currentTimeMillis() + TimeMillis.millisToMidnight());

        return returnCodes;
    }

    private void findIdsReactroles() {
        JDA jda = DiscordBot.INSTANCE.jda;
        SelfUser self = jda.getSelfUser();

        ResultSet set = LiteSQL.onQuery("SELECT * FROM reactroles");
        try {
            while (set.next()) {
                int id = set.getInt("id");
                long guildID = set.getLong("guildid");
                long channelID = set.getLong("channelid");
                long messageID = set.getLong("messageid");
                String emote = set.getString("emote");

                Guild guild = jda.getGuildById(guildID);

                try {
                    assert guild != null;
                    Objects.requireNonNull(guild.getTextChannelById(channelID)).retrieveReactionUsersById(messageID,emote).complete();
                }
                catch (NullPointerException | ErrorResponseException e) {saveId(id); continue;}

                try {
                    List<User> reactionUsers = Objects.requireNonNull(guild.getTextChannelById(channelID)).retrieveReactionUsersById(messageID, emote).complete();

                    if (!reactionUsers.contains(self)) { //Eintrag wird gelöscht wenn der Bot nicht mit dem Emote reagiert hat
                        saveId(id);
                    }
                } catch (InsufficientPermissionException e) {
                    returnCodes.add((byte) 1);
                } catch (IllegalArgumentException | ErrorResponseException e) { //Eintrag wird auch gelöscht wenn Emote oder Message nicht existiert
                    saveId(id);
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void findIdsVotereactions() {
        JDA jda = DiscordBot.INSTANCE.jda;
        ResultSet set = LiteSQL.onQuery("SELECT * FROM votereactions");

        try {
            while (set.next()) {
                int id = set.getInt("id");
                long guildID = set.getLong("guildid");
                long channelID = set.getLong("channelid");
                long messageID = set.getLong("messageid");

                if (jda.getGuildById(id) == null) saveId(id);

                try {
                    Objects.requireNonNull(Objects.requireNonNull(jda.getGuildById(guildID)).getTextChannelById(channelID))
                            .retrieveMessageById(messageID).complete();
                    new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> saveId(id))
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, (e) -> saveId(id))
                            .handle(ErrorResponse.MISSING_ACCESS, (e) -> saveId(id));
                }
                catch (InsufficientPermissionException e) {
                    returnCodes.add((byte) 1);
                }
                catch (ErrorResponseException e) {
                    saveId(id);
                }
                catch (NullPointerException e) {
                    //
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveId(int id) {
        System.out.println("saved id: " + id);
        lineIDs.add(id);
    }

    private void deleteLinesById(String table) {

        if (lineIDs != null) {

            for (int i = lineIDs.size(); i > 0; i--) {
                int lineID = lineIDs.get(i - 1);

                String sql = "DELETE FROM " + table + " WHERE id = " + lineID;

                LiteSQL.onUpdate(sql);

                System.out.println("deleted line with id: " + lineID);
            }
        }
    }
}
