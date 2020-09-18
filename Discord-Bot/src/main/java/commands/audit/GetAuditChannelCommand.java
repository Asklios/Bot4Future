package main.java.commands.audit;

import main.java.commands.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetAuditChannelCommand implements ServerCommand {

    // gibt den aktuell festgelegten Audit-Channel aus
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {


            try {
                long auditChannelId = this.channelDatabase.getAuditChannel(channel.getGuild()).getIdLong();//GuildDataXmlReadWrite.readAuditChannelId(member.getGuild().getIdLong());
                if (auditChannelId != 0) {
                    //String auditChannelID = channel.getGuild().getGuildChannelById(auditChannelId).getId();
                    channel.sendMessage("<#" + auditChannelId + "> ist der aktuelle Audit-Channel.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
                } else {

                    channel.sendMessage("Der Auditchannel ist noch nicht festgelegt.\n```%audit #channel```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
                }

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Cought Exception: NumberFormatException (GetAuditChannelCommand.java - performCommand)");
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
        }
    }
}


