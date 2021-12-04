package main.java.commands.server.developer;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import main.java.files.LiteSqlClear;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DbClearCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(member.getId())) {
            channel.sendMessage(member.getAsMention() + " Dieser Command ist nur für die Botentwickler*innen vorgesehen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        channel.sendTyping().queue();

        List<Byte> errorCodes = new LiteSqlClear().clearDatabase();

        if (errorCodes == null) {
            channel.sendMessage("Unbekannter Fehler.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            System.err.println("Unknown Error while trying to clear the database. (" + this.getClass().getName() +
            " line: " + Thread.currentThread().getStackTrace()[1].getLineNumber() + ")");
            return;
        }

        if (errorCodes.contains((byte) 1)) {
            channel.sendMessage("Der Bot hat auf mindestens einen Channel keinen Zugriff.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }

        if (errorCodes.contains((byte) 0)) {
            channel.sendMessage(member.getAsMention() + " Die Datenbank wurde bereinigt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }
}