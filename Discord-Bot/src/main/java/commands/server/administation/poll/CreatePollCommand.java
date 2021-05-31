package main.java.commands.server.administation.poll;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CreatePollCommand implements ServerCommand {
    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        DiscordBot.INSTANCE.pollManager.initPollSetupMessage(member, channel);
    }
}
