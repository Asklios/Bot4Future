package main.java.listener;

import main.java.DiscordBot;
import main.java.commands.server.administation.CookieCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import main.java.util.Emojis;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BumpListener extends ListenerAdapter {
    RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    Map<String, Integer> bumpers = new HashMap<>();

    private Random RANDOM = new Random();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getGuild() == null) return;
        if (roleDatabase.getBumpRole(event.getGuild()) == null) return;
        if (event.getAuthor().getIdLong() == 302050872383242240L //is disboard bot
                && event.getMessage().getEmbeds().size() == 1) { //message has embed
            MessageEmbed embed = event.getMessage().getEmbeds().get(0);
            String description = embed.getDescription();
            if (description == null) return;
            if (description.contains("https://disboard.org/") && embed.getImage() != null) {
                DiscordBot.POOL.schedule(() -> {
                            bumpers.put(event.getGuild().getId(), 0);
                            event.getChannel()
                                    .sendMessage(roleDatabase.getBumpRole(event.getGuild()).getAsMention()
                                            + ", ihr könnt wieder den Server bumpen!")
                                    .submit();
                        }
                        , 2, TimeUnit.HOURS);
            }
        }
        if (event.getMessage().getContentRaw().equalsIgnoreCase("!d bump")) {
            if (bumpers.containsKey(event.getGuild().getId())) {
                bumpers.put(event.getGuild().getId(), bumpers.get(event.getGuild().getId()) + 1);
                if (CookieCommand.cookieServers.contains(event.getGuild().getId())) {
                    int bumps = bumpers.get(event.getGuild().getId());
                    int cookies;
                    StringBuilder builder = new StringBuilder();
                    switch (bumps) {
                        case 1:
                            cookies = RANDOM.nextInt(10) + 15;
                            builder.append("Super " + event.getMember().getAsMention() + ", schön das " +
                                    "du unseren Server gebumpt hast! Dafür gebe ich dir " + cookies + " Kekse:\n");
                            for (int i = 0; i < cookies; i++) {
                                builder.append(Emojis.COOKIE);
                            }
                            event.getMessage().reply(builder.toString()).queue();
                            break;
                        case 2:
                            cookies = RANDOM.nextInt(10) + 5;
                            builder.append(event.getMember().getAsMention() + ", du warst nur der Zweite!!!! " +
                                    "Sei das nächste mal schneller, dann bekommst du mehr als " + cookies + " Kekse:\n");
                            for (int i = 0; i < cookies; i++) {
                                builder.append(Emojis.COOKIE);
                            }
                            event.getMessage().reply(builder.toString()).queue();
                            break;
                        case 3:
                            cookies = RANDOM.nextInt(5) + 1;
                            builder.append(event.getMember().getAsMention() + ", nur Dritter!!! Schäm dich!!!\n\n" +
                                    "Aber ich will mal nicht so sein, hier, " + cookies + " Trostkekse für dich:\n");
                            for (int i = 0; i < cookies; i++) {
                                builder.append(Emojis.COOKIE);
                            }
                            event.getMessage().reply(builder.toString()).queue();
                            break;
                    }
                }
            }
        }
    }
}
