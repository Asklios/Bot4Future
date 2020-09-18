package main.java.commands.administation;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.GetMemberFromMessageFind;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.*;
import main.java.temp.UnMuteBanCheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MuteCommand implements ServerCommand {


    private GetMemberFromMessage getMemberFromMessage = new GetMemberFromMessageFind();
    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();
    private UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();
    private ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.KICK_MEMBERS)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        channel.sendTyping().queue();

        //mute @User <time>
        String[] messageSplit = message.getContentRaw().split("\\s+");
        Guild guild = channel.getGuild();
        Role muteRole = this.guildDatabase.getMuteRole(guild);

        if (muteRole == null) {
            channel.sendMessage("Es wurde noch keine Muterolle festgelegt. `%muterole @role`").complete().delete().queueAfter(5,TimeUnit.SECONDS);
            return;
        }

        if (messageSplit.length == 0) {
            channel.sendMessage("`%mute @user <time> <reason>` ```<time> = <Anzahl><Einheit> \n \n " +
                    "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        if (messageSplit.length < 3) {
            channel.sendMessage("Falsche Formatierung. `%mute @user <time> <reason>` ```<time> = <Anzahl><Einheit> \n \n " +
                    "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").complete().delete().queueAfter(15,TimeUnit.SECONDS);
            return;
        }

        Member mention = this.getMemberFromMessage.firstMentionedMember(message);
        String timeString = messageSplit[2];
        long timeMillis = getTimeMillis(member, channel, timeString);
        long date = System.currentTimeMillis();
        long endtime = date + timeMillis;
        long guildID = guild.getIdLong();
        long muteUserID;
        String muteMemberRoles;
        String reason;

        //es konnte kein Member gefunden/gelesen werden
        if (mention == null) {
            channel.sendMessage(member.getAsMention() + " - Die UserId \"" + messageSplit[1] + "\" konnte nicht gefunden werden.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
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
                channel.sendMessage("Der Bot kann " + mention.getEffectiveName() + " nicht muten, da seine Rollen zu niedrig sind.")
                        .complete().delete().queueAfter(5, TimeUnit.SECONDS);
                return;
            }
        }

        muteMemberRoles = mention.getRoles().stream().map(ISnowflake::getIdLong).map(Object::toString).collect(Collectors.joining("⫠"));
        reason = Arrays.stream(messageSplit, 3, messageSplit.length).collect(Collectors.joining(" "));
        muteUserID = mention.getIdLong();

        this.userRecordsDatabase.addRecord(muteUserID, date, endtime, "mute", guildID, reason, muteMemberRoles);

        channel.sendMessage(mention.getAsMention() + " wurde für " + timeString + " gemuted.").complete().delete().queueAfter(10, TimeUnit.SECONDS);

        //inform user
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date endDate = new Date(endtime);
        User muteUser = mention.getUser();
        PrivateChannel privateChannel = muteUser.openPrivateChannel().complete();
        EmbedBuilder p = new EmbedBuilder();
        p.setTitle("Du wurdest auf " + guild.getName() + " gemuted. \n Server-ID: *" + message.getGuild().getId() + "*");
        p.setDescription("**Begründung:** " + reason + "\n \n Endet: " + sdf.format(endDate));
        p.setImage(message.getGuild().getBannerUrl());
        p.setTimestamp(OffsetDateTime.now());
        p.setColor(0xff000);

        privateChannel.sendMessage(p.build()).complete();

        // audit Message
        TextChannel audit = this.channelDatabase.getAuditChannel(guild);
        EmbedBuilder a = new EmbedBuilder();

        if (!(audit == null)) {

            a.setTitle(":mute: Nutzer gemuted");
            a.setColor(0xffcf00); //gelb
            a.setThumbnail(muteUser.getAvatarUrl() == null ? muteUser.getDefaultAvatarUrl() : muteUser.getAvatarUrl());
            a.setFooter("by " + message.getAuthor().getName());
            a.appendDescription("Dauer: " + timeString + " -> " + sdf.format(endDate));
            a.addField("Name: ", muteUser.getAsMention(), false);
            a.addField("ID: ", muteUser.getId(), false);
            a.addField(":page_facing_up:Begründung: ", reason, false);

            audit.sendMessage(a.build()).queue();
        }

        //execute mute

        for (Role r : removeRoles) {
            guild.removeRoleFromMember(mention,r).complete();
        }
        guild.addRoleToMember(mention, muteRole).complete();
        
        
        if(endtime - date < DiscordBot.INSTANCE.getMuteTimerPeriod()) {
        	
        	UnMuteBanCheck m = new UnMuteBanCheck();
        	m.setDefinitelyMuted(true);
        	m.run();
        }
        
    }

    private long getTimeMillis(Member member, TextChannel channel, String timeString) {
        long timeMillis;
        try {
            if (timeString.endsWith("m")) {
                timeMillis = TimeMillis.monthMillis(timeString);
            }
            else if (timeString.endsWith("w")) {
                timeMillis = TimeMillis.weekMillis(timeString);
            }
            else if (timeString.endsWith("d")) {
                timeMillis = TimeMillis.dayMillis(timeString);
            }
            else if (timeString.endsWith("h")) {
                timeMillis = TimeMillis.hourMillis(timeString);
            }
            else if (timeString.endsWith("min")) {
                timeMillis = TimeMillis.minuteMillis(timeString);
            }
            else if (timeString.endsWith("sec")) {
                timeMillis = TimeMillis.secondMillis(timeString);
            }
            else{
                sendMessageTimeFormat(member, channel);
                return 0;
            }
        } catch (NumberFormatException e) {
            sendMessageTimeFormat(member, channel);
            return 0;
        }
        return timeMillis;
    }

    private void sendMessageTimeFormat(Member member, TextChannel channel) {
        channel.sendMessage(member.getAsMention() + " Die Zeitangabe wurde falsch formatiert. ```<time> = <Anzahl><Einheit> \n \n " +
        "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
    }
}
