package main.java.listener;

import main.java.DiscordBot;
import main.java.commands.invite.SpecialCodeCommand;
import main.java.commands.pnSystem.UnbanRequestHandler;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentDisplay(); // Nachricht wie sie ankommt mit Formatierung

        // wenn die Nachricht aus einem Private-Channel stammt
        if (event.isFromType(ChannelType.PRIVATE)) {
            UnbanRequestHandler.handle(event);
        }


        // wenn die Nachricht aus einem Text-Channel von einem Server stammt
        if (event.isFromType(ChannelType.TEXT)) {
            TextChannel channel = event.getTextChannel();

            if (message.startsWith("%")) { // Festlegung des Präfix
                String[] args = message.substring(1).split("\\s+");
                if (args.length > 0) {
                    if (!DiscordBot.INSTANCE.getCmdMan().perform(args[0], event.getMember(), channel, event.getMessage())) {
                        channel.sendMessage("unknown command").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                    }
                }
                try {
                    event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (ErrorResponseException e) {
                    System.err.println("CommandListener.java --> [ErrorResponseException] " +
                            e.getErrorCode() + ": " + e.getMeaning());
                    System.out.println("Possible cause: trying to delete a message that was already gone");
                }
            } else {
                DiscordBot.INSTANCE.getAutoListener().autoListen(Objects.requireNonNull(event.getMember()),
                        channel, event.getMessage());
            }
        }
    }

    // Invite Manager
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        SpecialCodeCommand.guildMemberJoin(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("connected to: " + event.getGuild().getName());
        ArrayList<Guild> singleGuildList = new ArrayList<Guild>();
        singleGuildList.add(event.getGuild());

        DiscordBot.INSTANCE.updateGuilds(singleGuildList);
        SpecialCodeCommand.writeInviteCount(singleGuildList);
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("ready");
        DiscordBot.INSTANCE.updateGuilds(event.getJDA().getGuilds());
        SpecialCodeCommand.writeInviteCount(event.getJDA().getGuilds());
    }
}


