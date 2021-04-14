package main.java.commands.server.audit;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class EventAuditChannelCommand implements ServerCommand {

    Long guildID;
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        guildID = channel.getGuild().getIdLong();

        if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {// wenn der Nutzer die "Admin" Berechtigung hat

            String[] messageSplit = message.getContentDisplay().split("\\s+");

            // %eventaudit #audit
            if (messageSplit.length == 2) {
                try {
                    try {
                        this.channelDatabase.saveEventAuditChannel(message.getMentionedChannels().get(0));
                        channel.sendMessage("Das Event-Audit wird jetzt in " + message.getMentionedChannels().get(0).getAsMention() + " gesendet.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                    } catch (IndexOutOfBoundsException e) {
                        // System.err.println("Cought Exception: IndexOutOfBoundsException (AuditChannelCommand.java - performCommand)");
                        channel.sendMessage("Textchannel nicht gefunden.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Cought Exception: NumberFormatException (EventAuditChannelCommand.java - performCommand)");
                }
            } else {

                EmbedBuilder builder = new EmbedBuilder();
                channel.sendMessage("Falsche Formatierung!").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                builder.setDescription("%eventaudit #channel");
                channel.sendMessage(builder.build()).queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }
}

