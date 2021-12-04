package main.java.commands.server.question;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;

public class GetQuestionChannelCommand implements ServerCommand {

    // gibt den aktuell festgelegten Audit-Channel aus
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            try {
                long questionChannelId = this.channelDatabase.getQuestionChannel(channel.getGuild()).getIdLong();
                if (questionChannelId != 0) {
                    channel.sendMessage("<#" + questionChannelId + "> ist der aktuelle Umfragen-Channel.")
                            .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                } else {
                    channel.sendMessage("Der Umfragen-Channel ist noch nicht festgelegt.\n```%questions #channel```")
                            .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                }

            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Cought Exception: NumberFormatException (GetQuestionChannelCommand.java - performCommand)");
            }
            catch (NullPointerException e) {
                channel.sendMessage("Der Question-Channel ist noch nicht festgelegt.\n```%aquestions #channel```")
                        .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }
}


