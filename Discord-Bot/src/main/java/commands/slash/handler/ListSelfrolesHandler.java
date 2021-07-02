package main.java.commands.slash.handler;

import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ListSelfrolesHandler implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        HashMap<String, Long> server = IAmCommand.getServerSelfRoles().get(guildId);
        if(server == null){
            event.reply("Für diesen Server exsistieren keine selbst gebbaren Rollen.").setEphemeral(true).queue();
            return;
        }
        List<String> guildRoles = new ArrayList<>(IAmCommand.getServerSelfRoles().get(guildId).keySet());
        Collections.sort(guildRoles);
        if (guildRoles.isEmpty()) {
            event.reply("Für diesen Server exsistieren keine selbst gebbaren Rollen.").setEphemeral(true).queue();
        } else {
            String rollen = guildRoles.stream().collect(Collectors.joining(" \n"));
            String[] parts = rollen.split("(?<=\\G.{8})");
            for (String part : parts) {
                event.getHook().sendMessage(part).setEphemeral(true).complete();
            }
        }
    }

    @Override
    public boolean deferReply() {
        return true;
    }
}
