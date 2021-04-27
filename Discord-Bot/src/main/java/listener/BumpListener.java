package main.java.listener;

import main.java.DiscordBot;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BumpListener extends ListenerAdapter {
    RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (roleDatabase.getBumpRole(event.getGuild()) == null) return;
        if (event.getAuthor().getIdLong() == 302050872383242240L //is disboard bot
                && event.getMessage().getEmbeds().size() == 1) { //message has embed
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String description = embed.getDescription();
            if (description == null) return;
            if (description.contains("https://disboard.org/") && embed.getImage() != null) {
                DiscordBot.POOL.schedule(() ->
                                event.getChannel()
                                        .sendMessage(roleDatabase.getBumpRole(event.getGuild()).getAsMention() + ", ihr könnt wieder den Server bumpen!")
                                        .submit()
                        , 2, TimeUnit.HOURS);
            }
        }
    }
}
