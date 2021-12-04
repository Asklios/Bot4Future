package main.java.commands.server.administation.presence;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.CallDatabaseSQLite;
import main.java.files.interfaces.CallDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.TimeUnit;

public class RemoveCallDataCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        CallDatabase callDatabase = new CallDatabaseSQLite();

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du musst Administrator besitzen um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //%removeallcalldata
        long guildID = channel.getGuild().getIdLong();

        boolean successes = false;

        successes = callDatabase.removeGuildData(guildID);

        if (successes) channel.sendMessage(member.getAsMention() + " Alle Präsenzdaten für diesen Server wurden gelöscht.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
        else channel.sendMessage(member.getAsMention() + " Für diesen Server wurden noch keine Präsenzdaten gespeichert.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
