package main.java.listener;

import main.java.DiscordBot;
import main.java.activitylog.CryptoMessageHandler;
import main.java.activitylog.EventAudit;
import main.java.commands.server.invite.SpecialCodeCommand;
import main.java.commands.server.pmCommands.UnbanRequestHandler;
import main.java.helper.MuteObserver;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.getMember() != null)
            DiscordBot.INSTANCE.pollManager.handleMessageCreateEvent(event);

        String messageString = event.getMessage().getContentDisplay(); // Nachricht wie sie ankommt mit Formatierung
        String[] args = new String[0];
        try {
            args = messageString.substring(1).split("\\s+");
        } catch (StringIndexOutOfBoundsException e) {
            //message has no content (join message)
        }

        // wenn die Nachricht aus einem Private-Channel stammt
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (messageString.startsWith("%unban") || !messageString.startsWith("%")) {
                UnbanRequestHandler.handle(event);
                return;
            }

            PrivateChannel channel = event.getPrivateChannel();
            Message message = event.getMessage();
            User user = event.getAuthor();

            if (args.length > 0) {
                if (!DiscordBot.INSTANCE.getPrivCmdMan().perform(args[0], user, channel, message)) {
                    channel.sendMessage("unknown command").queue();
                }
            }
        }

        // wenn die Nachricht aus einem Text-Channel von einem Server stammt
        if (event.isFromType(ChannelType.TEXT)
                || event.isFromType(ChannelType.GUILD_NEWS_THREAD)
                || event.isFromType(ChannelType.GUILD_PUBLIC_THREAD)
                || event.isFromType(ChannelType.GUILD_PRIVATE_THREAD)) {
            GuildMessageChannel channel = event.getGuildChannel();

            new CryptoMessageHandler().saveNewMessage(messageString, event.getGuild().getIdLong(),
                    event.getChannel().getIdLong(), event.getMessage().getIdLong(),
                    Objects.requireNonNull(event.getMember()).getIdLong());

            if (messageString.startsWith("%")) { // Festlegung des Präfix
                if (args.length > 0) {
                    if (!DiscordBot.INSTANCE.getCmdMan().perform(args[0], event.getMember(), channel, event.getMessage())) {
                        channel.sendMessage("unknown command").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
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

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // Invite Manager
        SpecialCodeCommand.guildMemberJoin(event);

        //check if muted
        MuteObserver.guildMemberJoin(event);
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        //check if muteRole was removed
        MuteObserver.onGuildMemberUpdate(event);
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
        DiscordBot.INSTANCE.slashCommandManager.startupGuilds();
        DiscordBot.INSTANCE.selfRoles.cleanUp();
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        DiscordBot.INSTANCE.slashCommandManager.handleSlashCommand(event);
    }
}


