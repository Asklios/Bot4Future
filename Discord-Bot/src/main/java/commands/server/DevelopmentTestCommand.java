package main.java.commands.server;

import main.java.helper.api.UpdateFromApi;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class DevelopmentTestCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        new UpdateFromApi().completeUpdate();
    }
}
