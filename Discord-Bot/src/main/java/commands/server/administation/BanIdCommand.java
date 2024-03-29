package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class BanIdCommand implements ServerCommand {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.BAN_MEMBERS)) {
            channel.sendMessage("Du benötigst die Berechtigung Nutzer zu bannen um diesen Command nutzen zu können.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //[%banid <id> reason
        String[] messageSplit = message.getContentDisplay().split("\\s+");
        //String idString = null;
        long id = 0;
        Member banMember = null;

        try {
            try {
                id = Long.parseLong(messageSplit[1]);
            } catch (NumberFormatException e) {
                channel.sendMessage("Es wurde keine korrekte ID angegeben. Eine ID besteht aus 18 Zahlen. " +
                        "Möglicherweise wurde ein Leerzeichen zwischen ID und Begründung vergessen.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }

            banMember = channel.getGuild().retrieveMemberById(id).complete();

            if (banMember == null) {
                channel.sendMessage("Unter dieser ID könnte kein User gefunden werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
            channel.sendMessage("Es konnte keine gültige ID erkannt werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        if (messageSplit.length > 2) {

            int messageLength = messageSplit.length;
            String reason = " ";
            for (int i = messageLength - 1; i > 1; i--) {
                reason = String.join(messageSplit[i], " ", reason);
            }

            if (reason.equals(" ")) {
                channel.sendMessage("Es wurde keine Begründung angegeben. `%banid <ID> <reason>` ").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }

            if (id == (message.getAuthor().getIdLong())) {
                channel.sendMessage(message.getAuthor().getAsMention() + " Du willst dich selbst bannen? Wirklich? Ich mein wenn du das wünschst ließe sich das natürlich einrichten, "
                        + "aber dann musst du jemand andern bitten das für mich zu übernehmen. Ich mach das nicht, ich mein, beim nächten Mal bin ich dann dran... ")
                        .queue(m -> m.delete().queueAfter(20,TimeUnit.SECONDS));
            } else {
                banMember(message, banMember, reason);
                outputAuditMessage(banMember.getUser(), message.getAuthor(), reason, message.getGuild());
            }
        } else {
            channel.sendMessage("```%banid <ID> <reason>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }

    private void banMember(Message message, Member banMember, String reason) {

        if (banMember != null) {

            Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

            if (banMember.getRoles().isEmpty() || highestBotRole.canInteract(banMember.getRoles().get(0))) {

                try {

                    // Nutzer wird per PN informiert
                    EmbedBuilder pn = new EmbedBuilder();

                    pn.setTitle("Du wurdest auf " + message.getGuild().getName() + " gebannt. \n Server-ID: *" + message.getGuild().getId() + "*");

                    pn.setDescription("**Begründung:** " + reason + "\n \n Wenn du Einspruch einlegen möchtest, dann tritt bitte unserem Bot-Dev-Server bei damit der Bot weiterhin deine Nachrichten lesen kann. "
                            + "\n https://discord.gg/KumdM4e \n \n Stelle anschließend deinen Antrag auf Entbannung indem du hier ```%unban``` schreibst. \n \n Wir haben keinen Einfluss darauf ob der Server einen Entbannungsantrag akzeptiert/annimt.");

                    pn.setImage(message.getGuild().getBannerUrl());
                    pn.setTimestamp(OffsetDateTime.now());
                    pn.setColor(0xff000);

                    banMember.getUser().openPrivateChannel().queue(p -> {
                        p.sendMessage(MsgCreator.of(pn.build())).queue();
                        System.out.println("PN sent to " + banMember.getUser().getName() + " (" + banMember.getUser().getId() + ")");
                    });

                } catch (IllegalStateException | ErrorResponseException e) {
                    message.getChannel().sendMessage("Es konnte keine PN an den Nutzer gesendet werden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                } catch (IllegalMonitorStateException e) {
                    System.err.println("Cought Exception: IllegalMonitorStateException BanCommand.java (banMemberPN)");
                }

                //Nutzer wird gebannt
                message.getGuild().ban(banMember, 1, reason).queue();
                message.getChannel().sendMessage(banMember.getAsMention() + " wurde gebannt.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));

            } else {
                message.getChannel().sendMessage("Der Bot kann " + banMember.getAsMention() + " nicht bannen, da seine Rollen zu niedrig sind.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
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

        audit.sendMessage(MsgCreator.of(builder.build())).queue();
    }
}
