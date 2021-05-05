package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TempBanCommand implements ServerCommand {

    private final UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();
    private final ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.BAN_MEMBERS)) {
            channel.sendMessage("Du hast nicht die Berechtigung diesen Command zu nutzen :(").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //%tempban @user <time> reason
        String[] messageSplit = message.getContentRaw().split("\\s+");
        long endTime = System.currentTimeMillis() + getTimeMillis(member, channel, messageSplit[2]);
        String reason = Arrays.stream(messageSplit, 3, messageSplit.length).collect(Collectors.joining(" "));
        Member banMember = GetMemberFromMessage.firstMentionedMember(message);

        banMember(message, banMember, reason, endTime);
        UserRecord userRecord = null;

        try {
            userRecord = this.userRecordsDatabase.addRecord(banMember.getIdLong(), System.currentTimeMillis(), endTime, "tempban", channel.getGuild().getIdLong(),
                    reason, "active");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        assert userRecord != null;
        new TimedTasks().addTimedTask(TimedTask.TimedTaskType.UNBAN, endTime, userRecord.getId() + "");
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
                "m = Monat(e) \n w = Woche(n) \n d = Tag(e) \n h = Stunde(n) \n min = Minute(n)```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
    }

    private void banMember(Message message, Member banMember, String reason, long endtime) {

        if (banMember != null) {

            Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

            if (!banMember.getRoles().isEmpty() && !highestBotRole.canInteract(banMember.getRoles().get(0))) {
                message.getChannel().sendMessage("Der Bot kann " + banMember.getAsMention() +
                        " nicht bannen, da seine Rollen zu niedrig sind.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }

            try {
                // Nutzer wird per PN informiert
                EmbedBuilder pn = new EmbedBuilder();

                pn.setTitle("Du wurdest auf " + message.getGuild().getName() + " temporär gebannt. \n Server-ID: *" + message.getGuild().getId() + "*");
                pn.setDescription("**Entbannungsdatum:** " + TimeMillis.dateFromMillis(endtime) +
                        "\n \n **Begründung:** " + reason + "\n \n Wenn du Einspruch einlegen möchtest, " +
                        "dann tritt bitte unserem Bot-Dev-Server bei damit der Bot weiterhin deine Nachrichten lesen kann. "
                        + "\n https://discord.gg/KumdM4e \n \n Stelle anschließend deinen Antrag auf Entbannung indem du " +
                        "hier ```%unban``` schreibst. \n \n Wir haben keinen Einfluss darauf ob der Server einen Entbannungsantrag akzeptiert/annimt.");
                pn.setImage(message.getGuild().getBannerUrl());
                pn.setTimestamp(OffsetDateTime.now());
                pn.setColor(0xff000);

                banMember.getUser().openPrivateChannel().queue(p -> {
                    p.sendMessage(pn.build()).queue();
                    System.out.println("PN sent to " + banMember.getUser().getName() + " (" + banMember.getUser().getId() + ")");
                });

            } catch (IllegalStateException | ErrorResponseException e) {
                message.getChannel().sendMessage("Es konnte keine PN an den Nutzer gesendet werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            } catch (IllegalMonitorStateException e) {
                System.err.println("Cought Exception: IllegalMonitorStateException BanCommand.java (banMemberPN)");
            }

            //Audit Nachricht wird gesendet
            outputAuditMessage(banMember.getUser(), message.getAuthor(), reason, TimeMillis.dateFromMillis(endtime), message.getGuild());

            //Nutzer wird gebannt
            message.getGuild().ban(banMember, 1, reason).queue();
        }
    }

    private void outputAuditMessage(User targetUser, User commandUser, String reason, String endDate, Guild guild) {

        TextChannel audit = this.channelDatabase.getAuditChannel(guild);

        // Nachricht wird in den festgelegten auditChannel gesendet
        if (audit == null) return;

        EmbedBuilder builder = new EmbedBuilder();
        // Inhalt der Auditausgabe bei Ban

        //builder.setFooter(bannedBy);
        builder.setTitle(":hammer: Nutzer temporär gebannt:");
        builder.setTimestamp(OffsetDateTime.now());
        builder.setColor(0xff0000); // rot
        builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
        builder.setFooter("by " + commandUser.getName() + " using Bot4Future");
        builder.addField("Name: ", targetUser.getAsMention(), false);
        builder.addField("ID: ", targetUser.getId(), false);
        builder.addField(":page_facing_up:Begründung: ", reason, false);
        builder.addField("Endet: ", endDate, false);

        audit.sendMessage(builder.build()).queue();
    }
}
