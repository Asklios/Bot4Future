package main.java.commands.slash.handler;

import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashMap;

public class IamNotHandler implements SlashCommandHandler {
    private SelfRoles selfRoles = new SelfRolesSQLite();

    @Override
    public void handle(SlashCommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        HashMap<String, Long> guildRoles = IAmCommand.getServerSelfRoles().get(guildId);
        long roleId = 0;
        try {
            roleId = guildRoles.get(event.getOption("rolle").getAsRole().getName().toLowerCase());
        } catch (NullPointerException e) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Entweder diese Rolle exsistiert nicht, oder du bist nicht" +
                    " berechtigt diese dir selber zu geben.").build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Guild guild = event.getGuild();
        Role role = guild.getRoleById(roleId);

        if (role == null) {
            event.replyEmbeds(new EmbedBuilder().setDescription("Die Rolle mit der ID: "
                    + roleId + " kann nicht mehr gefunden werden. " +
                    "Sie wird aus der Datenbank enfernt.").build())
                    .setEphemeral(true)
                    .queue();
            selfRoles.removeSelfRoleByRoleId(guildId, roleId);
            return;
        }

        if (event.getMember().getRoles().contains(role)) {
            guild.removeRoleFromMember(event.getMember(), role).queue();
            event.replyEmbeds(new EmbedBuilder().setDescription(event.getMember().getAsMention() + ", " +
                    "du hast die Rolle `" + role.getName() + "` entfernt.").build())
                    .setEphemeral(true)
                    .queue();
        } else {
            event.replyEmbeds(new EmbedBuilder().setDescription("Du hast die Rolle `"
                    + role.getName() + "` gar nicht.").build())
                    .setEphemeral(true)
                    .queue();
        }
    }
}
