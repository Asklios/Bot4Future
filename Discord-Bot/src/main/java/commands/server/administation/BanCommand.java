package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.helper.GetMemberFromMessage;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements ServerCommand {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.BAN_MEMBERS)) {

            //[%ban <reason> % <Member1> <Member2> <Member 3>]
            String[] messageSplit = message.getContentRaw().split("%");
            String reason = messageSplit[1].replace("ban ", "");

            if (messageSplit.length > 2) {
                String mentions = messageSplit[2];
                List<Member> members = GetMemberFromMessage.allMemberMentionsAndIds(mentions, message);

                if (members.isEmpty()) {
                    channel.sendMessage("Es konnten keine Erwähnungen oder Ids erkannt werden. ```%ban <reason> % <@Member1> <@Member2> <@Member3>```")
                            .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                    return;
                }

                for (Member m : members) {
                    try {
                        if (m.getIdLong() == (message.getAuthor().getIdLong())) {
                            channel.sendMessage(message.getAuthor().getAsMention() +
                                    " Du willst dich selbst bannen? Wirklich?" +
                                    " Ich mein wenn du das wünschst ließe sich das natürlich einrichten, "
                                    + "aber dann musst du jemand andern bitten das für mich zu übernehmen. " +
                                    "Ich mach das nicht, ich mein, beim nächten Mal bin ich dann dran... ")
                                    .queue(me -> me.delete().queueAfter(20,TimeUnit.SECONDS));
                        } else {
                            banMember(message, m, reason);
                        }
                    } catch (NullPointerException e) {
                        channel.sendMessage("Ein Member konnte nicht gefunden werden. ")
                                .queue(me -> me.delete().queueAfter(3,TimeUnit.SECONDS));
                        System.out.println("user: " + m.getIdLong() + " not found. Skip Ban.");
                    }
                }
            } else {
                channel.sendMessage("```%ban <reason> % <@Member1> <@Member2> <@Member3>```")
                        .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("Du benötigst die Berechtigung Nutzer zu bannen um diesen Command nutzen zu können.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
        }
    }

    private void banMember(Message message, Member banMember, String reason) {

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

                pn.setTitle("Du wurdest auf " + message.getGuild().getName() + " gebannt. \n Server-ID: *" +
                        message.getGuild().getId() + "*");

                pn.setDescription("**Begründung:** " + reason + "\n \n " +
                        "Wenn du Einspruch einlegen möchtest, dann tritt bitte unserem Bot-Dev-Server bei, " +
                        "damit der Bot weiterhin deine Nachrichten lesen kann. " +
                        "\n https://discord.gg/KumdM4e \n \n " +
                        "Stelle anschließend deinen Antrag auf Entbannung indem du hier ```%unban``` schreibst. \n \n " +
                        "Wir haben keinen Einfluss darauf ob der Server einen Entbannungsantrag akzeptiert/annimt.");

                pn.setImage(message.getGuild().getBannerUrl());
                pn.setTimestamp(OffsetDateTime.now());
                pn.setColor(0xff000);

                try {
                    banMember.getUser().openPrivateChannel().queue(p -> {
                        p.sendMessage(pn.build()).queue();
                        System.out.println("PM sent to " + banMember.getUser().getName() +
                                " (" + banMember.getUser().getId() + ")");
                    });
                } catch (ErrorResponseException e) {
                    System.out.println("PM to " + banMember.getUser().getName() + " (" + banMember.getUser().getId() +
                            ") was not send. PMs are not allowed by this user.");
                }

            } catch (IllegalStateException | ErrorResponseException e) {
                message.getChannel().sendMessage("Es konnte keine PN an den Nutzer gesendet werden.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            } catch (IllegalMonitorStateException e) {
                System.err.println("Cought Exception: IllegalMonitorStateException BanCommand.java (banMemberPN)");
            }

            //Nutzer wird gebannt
            message.getGuild().ban(banMember, 1, reason).queue();
            message.getChannel().sendMessage(banMember.getAsMention() + " wurde gebannt.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));

            //Audit Nachricht wird gesendet
            outputAuditMessage(banMember.getUser(), message.getAuthor(), reason, message.getGuild());
        }
    }

    private void outputAuditMessage(User targetUser, User commandUser, String reason, Guild guild) {

        TextChannel audit = this.channelDatabase.getAuditChannel(guild);

        // Nachricht wird in den festgelegten auditChannel gesendet
        if (audit == null) return;

        EmbedBuilder builder = new EmbedBuilder();
        // Inhalt der Auditausgabe bei Ban

        //builder.setFooter(bannedBy);
        builder.setTimestamp(OffsetDateTime.now());
        builder.setColor(0xff0000); // rot
        builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl());
        builder.setFooter("by " + commandUser.getName() + " using Bot4Future");
        builder.addField("Name: ", targetUser.getAsMention(), false);
        builder.addField("ID: ", targetUser.getId(), false);
        builder.addField(":page_facing_up:Begründung: ", reason, false);
        builder.setTitle(":hammer: Nutzer gebannt:");

        audit.sendMessage(builder.build()).queue();
    }
}
