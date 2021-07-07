package main.java.listener;

import main.java.commands.server.administation.CookieCommand;
import main.java.util.Emojis;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class CookieListener extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (CookieCommand.cookieServers.contains(event.getGuild().getId())) {
            event.getMessage().addReaction(Emojis.COOKIE).queue();
        }
    }
}
