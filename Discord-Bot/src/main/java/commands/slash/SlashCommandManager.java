package main.java.commands.slash;

import main.java.DiscordBot;
import main.java.commands.slash.handler.IamHandler;
import main.java.commands.slash.handler.IamNotHandler;
import main.java.commands.slash.handler.ListSelfrolesHandler;
import main.java.commands.slash.handler.SelfrolesHandler;
import main.java.commands.slash.models.IamNotSlashCommand;
import main.java.commands.slash.models.IamSlashCommand;
import main.java.commands.slash.models.ListSelfrolesCommand;
import main.java.commands.slash.models.SelfrolesSlashCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandManager {
    private final Map<String, SlashCommandHandler> handlers = new HashMap<>();
    private final List<CommandData> commands = new ArrayList<>();
    private final ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    private final List<ChannelType> allowedChannelTypes = List.of(
            ChannelType.TEXT,
            ChannelType.GUILD_NEWS_THREAD,
            ChannelType.GUILD_PRIVATE_THREAD,
            ChannelType.GUILD_PUBLIC_THREAD
    );

    public SlashCommandManager() {
        registerHandlers();
        registerCommands();
    }

    private void registerHandlers() {
        handlers.put("iamnot", new IamNotHandler());
        handlers.put("iam", new IamHandler());
        handlers.put("listroles", new ListSelfrolesHandler());

        handlers.put("selfroles", new SelfrolesHandler());
    }

    private void registerCommands() {
        commands.add(new IamSlashCommand());
        commands.add(new ListSelfrolesCommand());
        commands.add(new IamNotSlashCommand());

        commands.add(new SelfrolesSlashCommand());
    }

    public boolean registerCommandsForGuild(Guild guild) {
        try {
            for (CommandData cmdData : commands) {
                Command cmd = guild.upsertCommand(cmdData).complete();
                if (!cmd.isDefaultEnabled()) {
                    List<CommandPrivilege> privileges = new ArrayList<>();
                    guild.getRoles().forEach(role -> {
                        if (role.hasPermission(Permission.MESSAGE_MANAGE) && !role.isManaged()) {
                            privileges.add(CommandPrivilege.enable(role));
                        }
                    });
                    guild.updateCommandPrivilegesById(cmd.getIdLong(), privileges).complete();
                }
            }
        } catch (PermissionException exception) {
            return false;
        } catch (ErrorResponseException exception) {
            return false;
        }
        return true;
    }

    public void handleSlashCommand(SlashCommandEvent event) {
        String commandName = event.getName();
        if (event.getSubcommandGroup() != null)
            commandName += " " + event.getSubcommandGroup();
        if (event.getSubcommandName() != null)
            commandName += " " + event.getSubcommandName();

        if (event.getChannel() == null || !allowedChannelTypes.contains(event.getChannelType())) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Slash-Commands werden" +
                            " nur in normalen Kanälen und Threads unterstützt.").build())
                    .setEphemeral(true).queue();
            return;
        }
        TextChannel c = channelDatabase.getEventAuditChannel(event.getGuild());
        if (c != null) {
            c.sendMessage(MsgCreator.of(new EmbedBuilder().setTitle("Slash-Command genutzt: " + commandName)
                    .addField("User", event.getMember().getUser().getAsTag(), false)
                    .addField("Argumente", getArgString(event), false)
                    .addField("Channel", event.getTextChannel().getAsMention(), false)
                    .setColor(Color.YELLOW)
            )).queue();
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
        System.out.println("Slash commands updated.");
    }
}
