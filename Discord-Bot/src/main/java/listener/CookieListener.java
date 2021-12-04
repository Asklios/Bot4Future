package main.java.listener;

import main.java.commands.server.administation.CookieCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;

public class CookieListener extends ListenerAdapter {
    Random RANDOM = new Random();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getGuild() == null) return;
        if (RANDOM.nextInt(20) + 1 > 4) return;
        if (CookieCommand.cookieServers.contains(event.getGuild().getId())) {
            //event.getMessage().addReaction(Emojis.COOKIE).queue();
        }
    }
}
