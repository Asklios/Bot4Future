package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.GetMemberFromMessageFind;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.GetMemberFromMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BanCommand implements ServerCommand {

    GetMemberFromMessage getMemberFromMessage = new GetMemberFromMessageFind();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.BAN_MEMBERS)) {

            //[%ban <reason> % <Member1> <Member2> <Member 3>]
            String[] messageSplit = message.getContentRaw().split("%");
            String reason = messageSplit[1].replace("ban ", "");

            if (messageSplit.length > 2) {
                String mentions = messageSplit[2];
                List<Member> members = this.getMemberFromMessage.allMemberMentionsAndIds(mentions, message);

                if (members.isEmpty()) {
                    channel.sendMessage("Es konnten keine Erwähnungen oder Ids erkannt werden. ```%ban <reason> % <@Member1> <@Member2> <@Member3>```").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                    return;
                }

                for (Member m : members) {
                    if (m.getIdLong() == (message.getAuthor().getIdLong())) {
                        channel.sendMessage(message.getAuthor().getAsMention() + " Du willst dich selbst bannen? Wirklich? Ich mein wenn du das wünschst ließe sich das natürlich einrichten, "
                                + "aber dann musst du jemand andern bitten das für mich zu übernehmen. Ich mach das nicht, ich mein, beim nächten Mal bin ich dann dran... ").complete().delete().queueAfter(20, TimeUnit.SECONDS);

                    } else {
                        banMember(message, m, reason);
                    }
                }
            } else {
                channel.sendMessage("```%ban <reason> % <@Member1> <@Member2> <@Member3>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            }
        } else {
            channel.sendMessage("Du benötigst die Berechtigung Nutzer zu bannen um diesen Command nutzen zu können.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
        }
    }

    private void banMember(Message message, Member banMember, String reason) {

        if (banMember != null) {

            Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

            if (banMember.getRoles().isEmpty() || highestBotRole.canInteract(banMember.getRoles().get(0))) {

                try {

                    // Nutzer wird per PN informiert
                    EmbedBuilder pn = new EmbedBuilder();

                    pn.setTitle("Du wurdest auf " + message.getGuild().getName().toString() + " gebannt. \n Server-ID: *" + message.getGuild().getId() + "*");

                    pn.setDescription("**Begründung:** " + reason + "\n \n Wenn du Einspruch einlegen möchtest, dann tritt bitte unserem Bot-Dev-Server bei damit der Bot weiterhin deine Nachrichten lesen kann. "
                            + "\n https://discord.gg/KumdM4e \n \n Stelle anschließend deinen Antrag auf Entbannung indem du hier ```%unban``` schreibst. \n \n Wir haben keinen Einfluss darauf ob der Server einen Entbannungsantrag akzeptiert/annimt.");

                    pn.setImage(message.getGuild().getBannerUrl());
                    pn.setTimestamp(OffsetDateTime.now());
                    pn.setColor(0xff000);

                    PrivateChannel ch = banMember.getUser().openPrivateChannel().complete();
                    ch.sendMessage(pn.build()).complete();
                    System.out.println("PN sent to " + banMember.getUser().getName() + " (" + banMember.getUser().getId() + ")");

                } catch (IllegalStateException | ErrorResponseException e) {
                    message.getChannel().sendMessage("Es konnte keine PN an den Nutzer gesendet werden.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (IllegalMonitorStateException e) {
                    System.err.println("Cought Exception: IllegalMonitorStateException BanCommand.java (banMemberPN)");
                }

                //Nutzer wird gebannt
                message.getGuild().ban(banMember, 1, reason).queue();

                //Audit Nachricht wird gesendet
                outputAuditMessage(banMember.getUser(), message.getAuthor(), reason, message.getGuild());

            } else {
                message.getChannel().sendMessage("Der Bot kann " + banMember.getAsMention() + " nicht bannen, da seine Rollen zu niedrig sind.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            }
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
        builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
        builder.setFooter("by " + commandUser.getName() + " using Bot4Future");
        builder.addField("Name: ", targetUser.getAsMention(), false);
        builder.addField("ID: ", targetUser.getId(), false);
        builder.addField(":page_facing_up:Begründung: ", reason, false);
        builder.setTitle(":hammer: Nutzer gebannt:");

        audit.sendMessage(builder.build()).queue();
    }
}
