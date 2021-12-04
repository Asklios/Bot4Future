package main.java.commands.server;

import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public interface ServerCommand {

    void performCommand(Member member, GuildMessageChannel channel, Message message);
}
