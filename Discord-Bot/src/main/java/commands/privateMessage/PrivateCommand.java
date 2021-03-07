package main.java.commands.privateMessage;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public interface PrivateCommand {
    void performCommand(User user, PrivateChannel channel, Message message);
}
