package main.java.commands.pnSystem;

import main.java.commands.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetPnChannelCommand implements ServerCommand {

    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {


            try {
                TextChannel pnChannel = this.channelDatabase.getPnChannel(channel.getGuild());
                if (pnChannel != null) {
                    //String auditChannelID = channel.getGuild().getGuildChannelById(auditChannelId).getId();
                    channel.sendMessage(pnChannel.getAsMention() + " ist der aktuelle PN-Channel.").complete().delete()
                            .queueAfter(10, TimeUnit.SECONDS);
                } else {
                    channel.sendMessage("Der PN-Channel ist noch nicht festgelegt.\n```%pnchannel #channel```").complete().delete()
                            .queueAfter(10, TimeUnit.SECONDS);
                }

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Cought Exception: NumberFormatException (GetPnChannelCommand.java - performCommand)");
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete()
                    .queueAfter(10, TimeUnit.SECONDS);
        }
    }
}