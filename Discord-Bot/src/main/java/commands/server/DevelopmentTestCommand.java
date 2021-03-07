package main.java.commands.server;

import main.java.helper.api.UpdateFromApi;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class DevelopmentTestCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        new UpdateFromApi().completeUpdate();
    }
}
