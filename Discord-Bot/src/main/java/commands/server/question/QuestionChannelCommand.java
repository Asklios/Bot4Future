package main.java.commands.server.question;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;

public class QuestionChannelCommand implements ServerCommand {
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        long guildID = channel.getGuild().getIdLong();

        if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {// wenn der Nutzer die "Admin" Berechtigung hat

            String[] messageSplit = message.getContentDisplay().split("\\s+");


            // %auditchannel #audit

            if (messageSplit.length == 2) {

                try {

                    try {
                        this.channelDatabase.saveQuestionChannel(message.getMentionedChannels().get(0));
                        channel.sendMessage("Bei Nachrichten in "
                                + message.getMentionedChannels().get(0).getAsMention()
                                + " wird ab jetzt immer mit den benutzten Emotes reagiert.").queue(m -> m.delete()
                                .queueAfter(10, TimeUnit.SECONDS));
                    } catch (IndexOutOfBoundsException e) {
                        // System.err.println("Cought Exception: IndexOutOfBoundsException (AuditChannelCommand.java - performCommand)");
                        channel.sendMessage("Textchannel nicht gefunden.")
                                .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Cought Exception: NumberFormatException (QuestionChannelCommand.java - performCommand)");
                }
            } else {

                EmbedBuilder builder = new EmbedBuilder();
                channel.sendMessage("Falsche Formatierung!").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                builder.setDescription("%questions #channel");
                channel.sendMessage(MsgCreator.of(builder)).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        }
    }
}

