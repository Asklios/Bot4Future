package main.java.commands.server.pmCommands;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class PmChannelCommand implements ServerCommand {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String[] messageSplit = message.getContentDisplay().split("\\s+");

        // %pmchannel #botpm
        if (messageSplit.length < 2) {

            channel.sendMessage("Falsche Formatierung! `%pnchannel #channel`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        try {

            try {
                this.channelDatabase.savePmChannel(message.getMentionedChannels().get(0));
                channel.sendMessage("Die PN-Nachrichten werden jetzt in " + message.getMentionedChannels().get(0).getAsMention() + " gesendet.")
                        .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            } catch (IndexOutOfBoundsException e) {
                channel.sendMessage("Textchannel nicht gefunden.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } catch (NumberFormatException e) {
            System.err.println("Cought Exception: NumberFormatException (PnChannelCommand.java - performCommand)");
        }
    }
}
