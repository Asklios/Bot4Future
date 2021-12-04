package main.java.commands.server.pmCommands;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetPmChannelCommand implements ServerCommand {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        try {
            TextChannel pnChannel = this.channelDatabase.getPmChannel(channel.getGuild());
            if (pnChannel != null) {

                channel.sendMessage(pnChannel.getAsMention() + " ist der aktuelle PM-Channel.")
                        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            } else {
                channel.sendMessage("Der PN-Channel ist noch nicht festgelegt.\n```%pmchannel #channel```")
                        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Cought Exception: NumberFormatException (GetPmChannelCommand.java - performCommand)");
        }
    }
}