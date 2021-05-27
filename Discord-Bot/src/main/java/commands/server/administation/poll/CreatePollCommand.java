package main.java.commands.server.administation.poll;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class CreatePollCommand implements ServerCommand {
    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if(!member.hasPermission(Permission.MESSAGE_MANAGE)){
            channel.sendMessage("Du hast nicht genügend Rechte dafür :(").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        DiscordBot.INSTANCE.pollManager.initPollSetupMessage(member, channel);
    }
}
