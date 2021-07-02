package main.java.commands.server.administation;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class RegisterSlashCommandsCommand implements ServerCommand {
    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            if (DiscordBot.INSTANCE.slashCommandManager.registerCommandsForGuild(channel.getGuild())) {
                message.reply("Die Slashcommands wurden erfolgreich zu diesem Server hinzugefügt.").queue();
            } else {
                message.reply("Ich habe leider nicht die benötigten Rechte, um Slashcommands zu" +
                        " diesem Server hinzuzufügen :(\nBitte melde dich bei Asklios#7916," +
                        " damit er den Bot die Rechte gibt.").queue();
            }
        } else {
            message.reply("Nur Admins können Slashcommands registrieren!").queue();
        }
    }
}
