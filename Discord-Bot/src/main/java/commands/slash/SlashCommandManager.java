package main.java.commands.slash;

import main.java.DiscordBot;
import main.java.activitylog.EventAudit;
import main.java.commands.slash.handler.IamHandler;
import main.java.commands.slash.handler.IamNotHandler;
import main.java.commands.slash.handler.ListSelfrolesHandler;
import main.java.commands.slash.models.IamNotSlashCommand;
import main.java.commands.slash.models.IamSlashCommand;
import main.java.commands.slash.models.ListSelfrolesCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlashCommandManager {
    private Map<String, SlashCommandHandler> handlers = new HashMap<>();
    private List<CommandData> commands = new ArrayList<>();
    private ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    public SlashCommandManager() {
        registerHandlers();
        registerCommands();
    }

    private void registerHandlers() {
        handlers.put("iamnot", new IamNotHandler());
        handlers.put("iam", new IamHandler());
        handlers.put("listroles", new ListSelfrolesHandler());
    }

    private void registerCommands() {
        commands.add(new IamSlashCommand());
        commands.add(new ListSelfrolesCommand());
        commands.add(new IamNotSlashCommand());
    }

    public boolean registerCommandsForGuild(Guild guild) {
        try {
            for (CommandData cmd : commands)
                guild.upsertCommand(cmd).complete();
        } catch (PermissionException exception) {
            return false;
        } catch (ErrorResponseException exception) {
            return false;
        }
        return true;
    }

    public void handleSlashCommand(SlashCommandEvent event) {
        if (event.getChannel() == null || event.getChannel().getType() != ChannelType.TEXT) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Slash-Commands werden" +
                    " nur in normalen Kanälen unterstützt. In Threads oder" +
                    " Direktnachrichten sind diese deaktiviert.").build())
                    .setEphemeral(true).queue();
            return;
        }
        TextChannel c = channelDatabase.getEventAuditChannel(event.getGuild());
        if (c != null) {
            c.sendMessage(new EmbedBuilder().setTitle("Slash-Command genutzt: " + event.getName())
                    .addField("User", event.getMember().getUser().getAsTag(), false)
                    .addField("Argumente", getArgString(event), false)
                    .addField("Channel", event.getTextChannel().getAsMention(), false)
                    .setColor(Color.YELLOW)
                    .build()).queue();
        }
        SlashCommandHandler handler = handlers.getOrDefault(event.getName(), null);
        if (handler == null) {
            event.reply("Für diesen Befehl ist kein Handler registriert :(\n" +
                    "Bitte melde dies einem Developer, das ist definitiv nicht so gedacht").setEphemeral(true)
                    .queue();
            return;
        } else {
            handler.handle(event);
        }
    }

    private String getArgString(SlashCommandEvent event) {
        String data = "";
        if (event.getOptions().size() == 0) return "*Keine genutzen Argumente*";
        for (OptionMapping mapping : event.getOptions()) {
            data += "**" + mapping.getName() + "**: ";
            switch (mapping.getType()) {
                case ROLE:
                    data += mapping.getAsRole().getAsMention();
                    break;
                case USER:
                    data += mapping.getAsUser().getAsMention();
                    break;
                case BOOLEAN:
                    data += mapping.getAsBoolean() ? "Ja" : "Nein";
                    break;
                case CHANNEL:
                    data += mapping.getAsGuildChannel().getAsMention();
                    break;
                case MENTIONABLE:
                    data += mapping.getAsMentionable().getAsMention();
                    break;
                default:
                    data += mapping.getAsString();
                    break;
            }
            data += "\n";
        }
        return data;
    }

    public void startupGuilds() {
        DiscordBot.INSTANCE.jda.getGuilds().forEach(guild -> {
            registerCommandsForGuild(guild);
        });
    }
}
