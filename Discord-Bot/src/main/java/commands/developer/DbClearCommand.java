package main.java.commands.developer;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import main.java.files.LiteSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DbClearCommand implements ServerCommand {

    List<Integer> lineIDs = new ArrayList<Integer>();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {


        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(member.getId())) {
            channel.sendMessage(member.getAsMention() + " Dieser Command ist nur für die Botentwickler*innen vorgesehen.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        channel.sendTyping().complete();

        findIdsReactroles(member, channel);
        deleteLinesById("reactroles");
        lineIDs.clear();
        System.out.println("cleared: reactroles");

        findIdsVotereactions(member,channel);
        deleteLinesById("votereactions");
        lineIDs.clear();
        System.out.println("cleared: votereactions");

        channel.sendMessage(member.getAsMention() + " Die Datenbank wurde bereinigt.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
    }

    private void findIdsReactroles(Member member, TextChannel channel) {
        JDA jda = channel.getGuild().getJDA();
        User self = jda.getSelfUser();

        ResultSet set = LiteSQL.onQuery("SELECT * FROM reactroles");
        try {
            while (set.next()) {
                int id = set.getInt("id");
                long guildID = set.getLong("guildid");
                long channelID = set.getLong("channelid");
                long messageID = set.getLong("messageid");
                String emote = set.getString("emote");

                try {
                    List<User> reactionUsers = jda.getGuildById(guildID).getTextChannelById(channelID).retrieveReactionUsersById(messageID, emote).complete();

                    if (!reactionUsers.contains(self)) { //Eintrag wird gelöscht wenn der Bot nicht mit dem Emote reagiert hat

                        saveId(id);

                    }
                } catch (InsufficientPermissionException e) {
                    channel.sendMessage(member.getAsMention() + " Der Bot hat auf einen channel keinen Zugriff.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (IllegalArgumentException e) { //Eintrag wird auch gelöscht wenn Emote oder Message nicht existiert
                    saveId(id);
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void findIdsVotereactions(Member member, TextChannel channel) {
        JDA jda = channel.getJDA();
        ResultSet set = LiteSQL.onQuery("SELECT * FROM votereactions");

        try {
            while (set.next()) {
                int id = set.getInt("id");
                long guildID = set.getLong("guildid");
                long channelID = set.getLong("channelid");
                long messageID = set.getLong("messageid");

                try {
                    jda.getGuildById(guildID).getTextChannelById(channelID).retrieveMessageById(messageID).complete();
                    new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> saveId(id))
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, (e) -> saveId(id))
                            .handle(ErrorResponse.MISSING_ACCESS, (e) -> saveId(id));
                }
                catch (InsufficientPermissionException e) {
                    channel.sendMessage(member.getAsMention() + " Der Bot hat auf einen channel keinen Zugriff.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
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