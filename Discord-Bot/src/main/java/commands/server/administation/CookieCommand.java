package main.java.commands.server.administation;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CookieCommand implements ServerCommand {
    public static List<String> cookieServers = new ArrayList<>();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            if (cookieServers.contains(member.getGuild().getId())) {
                cookieServers.remove(member.getGuild().getId());
                channel.sendMessage("Dieser Server ist nun nicht mehr im Cookie-Modus.")
                        .queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
            } else {
                cookieServers.add(member.getGuild().getId());
                channel.sendMessage("Dieser Server befindet sich nun für die nächsten 3 Tage bzw. bis zum" +
                        " nächsten Bot-Neustart im Coookie-Modus.")
                        .queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
                DiscordBot.POOL.schedule(() -> {
                    cookieServers.remove(member.getGuild().getId());
                }, 3, TimeUnit.DAYS);
            }
        } else {
            channel.sendMessage("Du hast nicht die erforderlichen Rechte um den Cookie-Modus zu aktivieren.")
                    .queue(msg -> msg.delete().queueAfter(10, TimeUnit.SECONDS));
        }
    }
}
