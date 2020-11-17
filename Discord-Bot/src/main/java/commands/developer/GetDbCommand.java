package main.java.commands.developer;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.Arrays;

public class GetDbCommand  implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(member.getId())) {
            return;
        }

        File dbFile = new File(DiscordBot.INSTANCE.getDbFilePath());
        member.getUser().openPrivateChannel().queue(p -> p.sendFile(dbFile).queue());
    }
}
