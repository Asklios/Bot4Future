package main.java.commands.slash.handler;

import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ListSelfrolesHandler implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        HashMap<String, Long> server = IAmCommand.getServerSelfRoles().get(guildId);
        if (server == null) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Für diesen Server" +
                                                                        " exsistieren keine selbst gebbaren Rollen.")
                                                .build())
                 .setEphemeral(true)
                 .queue();
            return;
        }
        List<Long> guildRoles = new ArrayList<>(new TreeMap<>(IAmCommand.getServerSelfRoles().get(guildId)).values());

        if (guildRoles.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Für diesen Server" +
                                                                        " exsistieren keine selbst gebbaren Rollen.")
                                                .build())
                 .setEphemeral(true)
                 .queue();
        } else {
            List<String> columns = new ArrayList<>();
            columns.add("");
            for (Long role : guildRoles) {
                String currentColumn = columns.get(columns.size() - 1);
                if (currentColumn.equals("")) currentColumn = "<@&" + role + ">";
                else
                    currentColumn += ", <@&" + role + ">";
                if (currentColumn.length() > 1024) {
                    columns.add("<@&" + role + ">");
                } else {
                    columns.set(columns.size() - 1, currentColumn);
                }
            }
            List<EmbedBuilder> builders = new ArrayList<>();
            builders.add(new EmbedBuilder()
                                 .setTitle("Selbst gebbare Rollen (" + guildRoles.size() + ")"));
            for (String column : columns) {
                EmbedBuilder builder = builders.get(builders.size() - 1);
                if (builder.getFields().size() == 25) {
                    builders.add(new EmbedBuilder()
                                         .setTitle("Selbst gebbare Rollen (" + guildRoles.size() + ")")
                                         .addField("", column, true)
                    );
                } else {
                    builder.addField("", column, true);
                }
            }
            event.replyEmbeds(builders.stream().map(EmbedBuilder::build).collect(Collectors.toList()))
                 .setEphemeral(true)
                 .queue();
        }
    }
}
