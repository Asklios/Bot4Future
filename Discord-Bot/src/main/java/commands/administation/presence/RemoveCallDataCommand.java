package main.java.commands.administation.presence;

import main.java.commands.ServerCommand;
import main.java.files.CallDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class RemoveCallDataCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {


        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du musst Administrator besitzen um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //%removeallcalldata
        long guildID = channel.getGuild().getIdLong();

        boolean successes = false;

        successes = CallDatabase.removeGuildData(guildID);

        if (successes) channel.sendMessage(member.getAsMention() + " Alle Präsenzdaten für diesen Server wurden gelöscht.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
        else channel.sendMessage(member.getAsMention() + " Für diesen Server wurden noch keine Präsenzdaten gespeichert.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
