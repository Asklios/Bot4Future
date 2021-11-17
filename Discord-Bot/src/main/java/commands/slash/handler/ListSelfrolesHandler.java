package main.java.commands.slash.handler;

import com.sun.source.tree.Tree;
import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import main.java.helper.api.LocalGroups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ListSelfrolesHandler implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        HashMap<String, Long> server = IAmCommand.getServerSelfRoles().get(guildId);
        if (server == null) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Für diesen Server" +
                            " exsistieren keine selbst gebbaren Rollen.").build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        List<Long> guildRoles = new ArrayList<>(new TreeMap<>(IAmCommand.getServerSelfRoles().get(guildId)).values());


        if (guildRoles.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Für diesen Server" +
                            " exsistieren keine selbst gebbaren Rollen.").build())
                    .setEphemeral(true)
                    .queue();
        } else {
            int fieldLength = 60;
            List<String>[] colums = new List[guildRoles.size() / fieldLength + 2];

            int j = 0;
            int k = 0;
            for (int i = 0; i < colums.length; i++) {
                colums[i] = new ArrayList();
                while (j * fieldLength > k) {
                    if (k >= guildRoles.size()) break;
                    colums[i].add( "<@&" + guildRoles.get(k) + ">");
                    k++;
                }
                j++;
            }

            EmbedBuilder b = new EmbedBuilder();
            b.setTitle("Selbst gebbare Rollen (" + guildRoles.size() + ")");
            for (int i = 1; i < colums.length; ) {
                while (b.length() < 5000) {
                    if (i >= colums.length) break;
                    List<String> l = colums[i];
                    b.addField("", String.join("\n", l), true);
                    i++;
                }
                event.replyEmbeds(b.build()).setEphemeral(true).queue();
                b.clearFields();
            }
        }
    }
}
