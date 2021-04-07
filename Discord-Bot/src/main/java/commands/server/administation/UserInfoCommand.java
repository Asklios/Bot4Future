package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.UserRecordsDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class UserInfoCommand implements ServerCommand {

    private final UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.KICK_MEMBERS)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        channel.sendTyping().queue();

        Guild guild = channel.getGuild();
        Member mention = null;
        try {
            mention = message.getMentionedMembers().get(0);
        } catch (IndexOutOfBoundsException e) {
            // Wenn kein Nutzer erwähnt wird
        }

        String[] messageSplit = message.getContentDisplay().split("\\s+");

        if (messageSplit.length > 1 && !message.getMentionedMembers().isEmpty()) {

            if (mention != null) {
                onInfo(member, mention, channel);
            } else {
                System.out.println(mention.getId());
                channel.sendMessage(member.getAsMention() + " - " + mention.getAsMention() + " konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }

        }
        else if (messageSplit.length > 1) {
            Member idMention;
            try {
                idMention = guild.retrieveMemberById(messageSplit[1]).complete();
            } catch (NullPointerException | NumberFormatException e) {
                channel.sendMessage(member.getAsMention() + " - Der User \"" + messageSplit[1] + "\" konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                return;
            }

            if (idMention == null) {
                channel.sendMessage("Falsche Formatierung: `%userinfo @user` (ID konnte nicht gelesen werden)")
                        .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                return;
            }

            if (member.getGuild().retrieveMemberById(idMention.getIdLong()).complete() != null) {
                onInfo(member, idMention, channel);
            } else {
                channel.sendMessage(member.getAsMention() + " - \"" + messageSplit[1] + "\" konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }

        }
        else {
            channel.sendMessage("Falsche Formatierung: `%userinfo @user`").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }


    private void onInfo(Member requester, Member user, TextChannel channel) {


        OffsetDateTime usercreated = user.getTimeCreated();
        OffsetDateTime userjoined = user.getTimeJoined();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        String formatUserJoined = userjoined.format(dateFormat);
        String formatUserCreated = usercreated.format(dateFormat);

        String userPermissions = user.getPermissions().stream().map(s -> new StringBuffer(s.getName())).collect(Collectors.joining("; "));

        if (userPermissions.contains("Administrator")) {
            userPermissions = "***Administrator***";
        }

        //.db request
        Map<String,Integer> records = null;
        int activeBans;
        int activeWarnings;
        int activeMutes;
        int totalBans;
        int totalMutes;

        records = userRecordsDatabase.recordNumbers(user.getIdLong());

        if (records == null){
            activeBans = 0;
            activeWarnings = 0;
            activeMutes = 0;
            totalBans = 0;
            totalMutes = 0;
        }
        else {
            activeBans = records.get("ban");
            activeWarnings = records.get("warning");
            activeMutes = records.get("mute");
            totalBans = records.get("bantotal");
            totalMutes = records.get("mutetotal");
        }

        //Ausgabe der UserInfo Nachricht
        EmbedBuilder builder = new EmbedBuilder();
        builder.setFooter("Requested by " + requester.getGuild().retrieveMemberById(requester.getIdLong()).complete().getEffectiveName());
        if (user.getColor() != null)
            builder.setColor(user.getColor());
        else
            builder.setColor(0x1da64a);
        builder.setTimestamp(OffsetDateTime.now());
        builder.setThumbnail(user.getUser().getEffectiveAvatarUrl());
        builder.setTitle(user.getEffectiveName(), user.getUser().getEffectiveAvatarUrl());

        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append("**User:** " + user.getAsMention() + "\n");
        strBuilder.append("**ClientID:** " + user.getId() + "\n");
        strBuilder.append("\n");
        strBuilder.append("**TimeJoined:** " + formatUserJoined + "\n");
        strBuilder.append("**TimeCreated:** " + formatUserCreated + "\n");
        strBuilder.append("**Permissions:** " + userPermissions + "\n");
        strBuilder.append("\n");
        strBuilder.append("Anzahl der Verwarnungen: " + activeWarnings + "\n");
        strBuilder.append("Anzahl aktiver Mutes: " + activeMutes + "\n");
        strBuilder.append("Anzahl aktiver Bans: " + activeBans + "\n \n");
        strBuilder.append("Anzahl aller Mutes: " + totalMutes + "\n");
        strBuilder.append("Anzahl aller Bans: " + totalBans + "\n");

        strBuilder.append(" \n *Rollen:* \n");

        StringBuilder roleBuilder = new StringBuilder();
        for (Role role : user.getRoles()) {
            roleBuilder.append(role.getAsMention() + " ");
        }
        strBuilder.append(roleBuilder.toString().trim() + "\n");

        builder.setDescription(strBuilder);

        channel.sendMessage(builder.build()).queue(m -> m.delete().queueAfter(60,TimeUnit.SECONDS));

    }
}

