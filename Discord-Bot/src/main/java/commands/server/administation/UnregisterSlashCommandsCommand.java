package main.java.commands.server.administation;

import main.java.DiscordBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class UnregisterSlashCommandsCommand implements main.java.commands.server.ServerCommand {
    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            DiscordBot.INSTANCE.slashCommandManager.unregisterCommandsForGuild(member.getGuild());
            message.reply("Die Slashcommands wurden von diesem Server wieder entfernt.").queue();
        } else {
            message.reply("Nur Admins können Slashcommands entfernen!").queue();
        }
    }
}
