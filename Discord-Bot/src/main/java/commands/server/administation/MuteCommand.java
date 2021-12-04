package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.RoleDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.*;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MuteCommand implements ServerCommand {

    private RoleDatabase roleDatabase = new RoleDatabaseSQLite();
    private UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();
    private ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.KICK_MEMBERS)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        channel.sendTyping().queue();

        //mute @User <time>
        String[] messageSplit = message.getContentRaw().split("\\s+");
        Guild guild = channel.getGuild();
        Role muteRole = this.roleDatabase.getMuteRole(guild);

        if (muteRole == null) {
            channel.sendMessage("Es wurde noch keine Muterolle festgelegt. `%muterole @role`").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        if (messageSplit.length == 0) {
            channel.sendMessage("`%mute @user <time> <reason>` ```<time> = <Anzahl><Einheit> \n \n " +
                    "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        if (messageSplit.length < 3) {
            channel.sendMessage("Falsche Formatierung. `%mute @user <time> <reason>` ```<time> = <Anzahl><Einheit> \n \n " +
                    "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").queue(m -> m.delete().queueAfter(15, TimeUnit.SECONDS));
            return;
        }

        Member mention = GetMemberFromMessage.firstMentionedMember(message); //getMemberFromMessage.firstMentionedMember(message);
        String timeString = messageSplit[2];
        long timeMillis = getTimeMillis(member, channel, timeString);
        long date = System.currentTimeMillis();
        long endTime = date + timeMillis;
        long guildId = guild.getIdLong();
        long muteUserId;
        String muteMemberRoles;
        String reason;

        //es konnte kein Member gefunden/gelesen werden
        if (mention == null) {
            channel.sendMessage(member.getAsMention() + " - Die UserId \"" + messageSplit[1] + "\" konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        //es konnte keine Zeit gefunden/gelesen werden
        if (timeMillis == 0) {
            sendMessageTimeFormat(member, channel);
            return;
        }

        //es fehlen Berechtigungen
        List<Role> removeRoles = mention.getRoles();
        for (Role r : removeRoles) {
            if (!guild.getSelfMember().getRoles().get(0).canInteract(r)) {
                try {
                    channel.sendMessage("Der Bot kann " + mention.getEffectiveName() + " nicht muten, da seine Rollen zu niedrig sind.")
                            .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                } catch (ErrorResponseException error) {
                    System.out.println("Error while removing roles!");
                }
                return;
            }
        }

        muteMemberRoles = mention.getRoles().stream().map(ISnowflake::getIdLong).map(Object::toString).collect(Collectors.joining("⫠"));
        reason = Arrays.stream(messageSplit, 3, messageSplit.length).collect(Collectors.joining(" "));
        muteUserId = mention.getIdLong();

        UserRecord userRecord = this.userRecordsDatabase.addRecord(muteUserId, date, endTime, "mute", guildId, reason, muteMemberRoles);

        channel.sendMessage(mention.getAsMention() + " wurde für " + timeString + " gemuted.").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));

        //inform user
        User muteUser = mention.getUser();

        muteUser.openPrivateChannel().queue(priv -> {

            EmbedBuilder p = new EmbedBuilder();
            p.setTitle("Du wurdest auf " + guild.getName() + " gemuted. \n Server-ID: *" + message.getGuild().getId() + "*");
            p.setDescription("**Begründung:** " + reason + "\n \n Endet: " + TimeMillis.dateFromMillis(endTime));
            p.setImage(message.getGuild().getBannerUrl());
            p.setTimestamp(OffsetDateTime.now());
            p.setColor(0xff000);

            priv.sendMessage(MsgCreator.of(p)).queue();
        });

        // audit Message
        TextChannel audit = this.channelDatabase.getAuditChannel(guild);
        EmbedBuilder a = new EmbedBuilder();

        if (!(audit == null)) {

            a.setTitle(":mute: Nutzer gemuted");
            a.setColor(0xffcf00); //gelb
            a.setThumbnail(muteUser.getAvatarUrl() == null ? muteUser.getDefaultAvatarUrl() : muteUser.getAvatarUrl());
            a.setFooter("by " + message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl());
            a.setTimestamp(OffsetDateTime.now());
            a.appendDescription("Dauer: " + timeString + " -> " + TimeMillis.dateFromMillis(endTime));
            a.addField("Name: ", muteUser.getAsMention(), true);
            a.addField("ID: ", muteUser.getId(), true);
            a.addField(":page_facing_up: Begründung: ", reason, false);

            audit.sendMessage(MsgCreator.of(a)).queue();
        }

        //execute mute

        for (Role r : removeRoles) {
            guild.removeRoleFromMember(mention, r).complete();
        }
        guild.addRoleToMember(mention, muteRole).complete();

        new TimedTasks().addTimedTask(TimedTask.TimedTaskType.UNMUTE, endTime, userRecord.getId() + "");
    }

    private long getTimeMillis(Member member, GuildMessageChannel channel, String timeString) {
        long timeMillis;
        try {
            if (timeString.endsWith("m")) {
                timeMillis = TimeMillis.monthMillis(timeString);
            } else if (timeString.endsWith("w")) {
                timeMillis = TimeMillis.weekMillis(timeString);
            } else if (timeString.endsWith("d")) {
                timeMillis = TimeMillis.dayMillis(timeString);
            } else if (timeString.endsWith("h")) {
                timeMillis = TimeMillis.hourMillis(timeString);
            } else if (timeString.endsWith("min")) {
                timeMillis = TimeMillis.minuteMillis(timeString);
            } else if (timeString.endsWith("sec")) {
                timeMillis = TimeMillis.secondMillis(timeString);
            } else {
                sendMessageTimeFormat(member, channel);
                return 0;
            }
        } catch (NumberFormatException e) {
            sendMessageTimeFormat(member, channel);
            return 0;
        }
        return timeMillis;
    }

    private void sendMessageTimeFormat(Member member, GuildMessageChannel channel) {
        channel.sendMessage(member.getAsMention() + " Die Zeitangabe wurde falsch formatiert. ```<time> = <Anzahl><Einheit> \n \n " +
                "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
    }
}
