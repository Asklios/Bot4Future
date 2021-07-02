package main.java.commands.slash;

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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void unregisterCommandsForGuild(Guild guild) {
        guild.retrieveCommands().queue(cmdList -> {
            for (Command cmd : cmdList)
                guild.deleteCommandById(cmd.getId()).queue();
        });
    }

    public void handleSlashCommand(SlashCommandEvent event) {
        if (event.getGuild() == null) return;
        TextChannel c = channelDatabase.getEventAuditChannel(event.getGuild());
        if(c != null){
            c.sendMessage(new EmbedBuilder().setTitle("Slash-Command genutzt:" + event.getName())
                    .addField("User", event.getMember().getUser().getAsTag(), false)
                    .addField("Befehl", event.toString(), false)
                    .addField("Channel", "#" + event.getChannel().getName(), false)
                    .setColor(Color.YELLOW)
            .build()).queue();
        }
        SlashCommandHandler handler = handlers.getOrDefault(event.getName(), null);
        if (handler == null) {
            event.reply("Für diesen Befehl ist kein Handler registriert :(\n" +
                    "Bitte melde dies einem Developer, das ist definitiv nicht so gedacht").setEphemeral(true)
                    .queue();
            return;
        }
        if (handler.deferReply()) event.deferReply().queue();
        handler.handle(event);
    }
}
