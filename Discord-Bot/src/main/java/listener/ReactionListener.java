package main.java.listener;

import main.java.DiscordBot;
import main.java.files.LiteSQL;
import main.java.files.interfaces.VoteDatabase;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ReactionListener extends ListenerAdapter {

    private final VoteDatabase voteDatabase;

    public ReactionListener(VoteDatabase voteDatabase) {
        this.voteDatabase = voteDatabase;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getChannelType() != ChannelType.TEXT) {
            return;
        }

        if (event.getUser().isBot()) {
            return;
        }

        if (event.isFromGuild()) {
            DiscordBot.INSTANCE.pollManager.handleReactionEvent(event);
        }
        Guild guild = event.getGuild();
        long guildID = event.getGuild().getIdLong();
        long channelID = event.getChannel().getIdLong();
        long messageID = event.getMessageIdLong();
        String emote;
        try {
            emote = event.getReactionEmote().getEmoji();
        } catch (IllegalStateException e) {
            return;
            //custom Emote
        }

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();

            //reactionroles
            ResultSet setRR = stmt.executeQuery("SELECT roleid FROM reactroles WHERE guildid = " +
                    guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " And emote = '" + emote + "'");

            if (setRR.next()) {
                long roleID = setRR.getLong("roleid");

                guild.addRoleToMember(event.getMember(), guild.getRoleById(roleID)).queue();
                guild.getTextChannelById(channelID).sendTyping();
            }
            setRR.close();

            //self reactions
            if (emote.equals("✅")) {//unban

                ResultSet setSelf = stmt.executeQuery("SELECT bannedid FROM unbanhandlerreactions WHERE guildid = " +
                        guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);

                try {
                    if (setSelf.next()) {
                        long bannedID = setRR.getLong("bannedid");

                        guild.unban(bannedID + "").queue();

                        LiteSQL.onUpdate("DELETE FROM unbanhandlerreactions WHERE guildid = " +
                                guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);
                    }
                    setSelf.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ErrorResponseException e) {
                    //Ban ist unbekannt
                } finally {
                }
            } else if (emote.equals("❌")) {//leave banned
                LiteSQL.onUpdate("DELETE FROM unbanhandlerreactions WHERE guildid = " +
                        guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);
            }

            stmt.close();
            connection.close();
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        } catch (HierarchyException e) {
            event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht vergeben, da sie höher als die Bot-Rolle ist.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        } catch (IllegalArgumentException e) {
            event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht vergeben, da sie nicht mehr existiert.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }

        reactPollAdd(event, guild, guildID, channelID, messageID, emote);
    }

    private void reactPollAdd(MessageReactionAddEvent event, Guild guild, long guildID, long channelID, long messageID, String emote) {

        User user = event.getUser();
        if (user == null) return;

        if (this.voteDatabase.isEmote(guildID, channelID, messageID, emote)) {
            event.getReaction().removeReaction(user).queue();
            return;
        }

        if (this.voteDatabase.hasVoted(guildID, channelID, messageID, event.getUserIdLong())) {
            event.getReaction().removeReaction(user).queue();
            return;
        }

        try {
            this.voteDatabase.addVote(guildID, channelID, messageID, user.getIdLong(), emote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {

        if (event.getChannelType() != ChannelType.TEXT) {
            return;
        }

        if (Objects.requireNonNull(event.getUser()).isBot()) {
            return;
        }

        DiscordBot.INSTANCE.pollManager.handleReactionEvent(event);
        //reactionroles
        long guildID = event.getGuild().getIdLong();
        long channelID = event.getChannel().getIdLong();
        long messageID = event.getMessageIdLong();
        String emote;
        try {
            emote = event.getReactionEmote().getEmoji();
        } catch (IllegalStateException e) {
            return;
            //custom Emote
        }
        Guild guild = event.getGuild();

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT roleid FROM reactroles WHERE guildid = " +
                    guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " And emote = '" + emote + "'");

            if (set.next()) {
                long roleID = set.getLong("roleid");

                guild.removeRoleFromMember(event.getMember(), guild.getRoleById(roleID)).queue();
                guild.getTextChannelById(channelID).sendTyping();
            }
            set.close();
            statement.close();
            connection.close();
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        } catch (HierarchyException e) {
            event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht entziehen, da sie höher als die Bot-Rolle ist.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        } catch (IllegalArgumentException e) {
            // Reaktion von einer gelöschten Rolle wird entfernt
        }

        reactPollRemove(event, guild, guildID, channelID, messageID, emote);
    }

    private void reactPollRemove(MessageReactionRemoveEvent event, Guild guild, long guildID, long channelID, long messageID, String emote) {
        long reactions = event.retrieveMessage().complete().getReactions().stream()
                .map(messageReaction -> messageReaction.retrieveUsers().stream().anyMatch(user -> user.getIdLong() == event.getUserIdLong()))
                .filter(aBoolean -> aBoolean)
                .count(); //Anzahl der Reaktionen des Users

        if (reactions > 0) return;

        if (!this.voteDatabase.isEmote(guildID, channelID, messageID, emote)) return;

        //remove Vote from Database
        try {
            this.voteDatabase.removeVote(guildID, channelID, messageID, event.getUserIdLong(), emote);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
