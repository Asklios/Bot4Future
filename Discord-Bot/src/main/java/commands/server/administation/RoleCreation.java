package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.concurrent.TimeUnit;


public class RoleCreation implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        // %createrole <Name> #99AAB5

        if (member.hasPermission(channel, Permission.MANAGE_ROLES)) {

            Guild guild = channel.getGuild();
            String[] args = message.getContentDisplay().split("\\s+");
            int length = args.length;

            if (length == 2 || length == 3) {
                StringBuilder builder = new StringBuilder();

                if (args[length - 1].startsWith("#") && length > 2) {
                    for (int i = 1; i < length - 1; i++) builder.append(args[i] + " ");
                    String hexCode = args[length - 1];

                    String roleName = builder.toString().trim();
                    channel.sendTyping().queue(); // Anzeige Bot tippt (max. 5sek)

                    guild.createRole().queue(role -> {
                        Color color = (Color.decode(hexCode));
                        role.getManager().setName(roleName).setColor(color).setPermissions().queue(); // Permissions in der setPermission Klammer werden der Rolle gegeben
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setDescription("Die Rolle " + roleName + " wurde erstellt."); // Antwort auf Rolle erstellt
                        embed.setColor(color);
                        channel.sendMessage(MsgCreator.of(embed)).queue();
                    });
                } else {
                    for (int i = 1; i < length; i++) builder.append(args[i] + " ");

                    String roleName = builder.toString().trim();
                    channel.sendTyping().queue();

                    guild.createRole().queue(role -> {
                        Color color = new Color(0x1da64a);
                        role.getManager().setName(roleName).setColor(color).setPermissions().queue(); // Permissions in der setPermission Klammer werden der Rolle gegeben
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setDescription("Die Rolle " + roleName + " wurde erstellt."); // Antwort auf Rolle erstellt
                        embed.setColor(color);
                        channel.sendMessage(MsgCreator.of(embed)).queue();
                    });
                }

            } else {
                channel.sendMessage("Falsche Formatierung!").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                EmbedBuilder builder = new EmbedBuilder();
                builder.setDescription("%createrole <Name> (<#FarbeHex>)"); // Antwort auf nur %createrole
                channel.sendMessage(MsgCreator.of(builder)).queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }
            //message.delete().queue();
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }
}
