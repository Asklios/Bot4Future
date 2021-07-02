package main.java.commands.slash.handler;

import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashMap;

public class IamHandler implements SlashCommandHandler {
    private SelfRoles selfRoles = new SelfRolesSQLite();

    @Override
    public void handle(SlashCommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        HashMap<String, Long> guildRoles = IAmCommand.getServerSelfRoles().get(guildId);
        long roleId = 0;
        try {
            roleId = guildRoles.get(event.getOption("rolle").getAsRole().getName().toLowerCase());
        } catch (NullPointerException e) {
            event.reply("Diese Rolle kannst du dir nicht selbst geben oder sie existiert nicht.")
                    .setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        Role role = guild.getRoleById(roleId);

        if (role == null) {
            event.reply("Die Rolle mit der ID: " + roleId + " kann nicht mehr gefunden werden. " +
                    "Sie wird aus der Datenbank enfernt.").setEphemeral(true)
                    .queue();
            selfRoles.removeSelfRoleByRoleId(guildId, roleId);
            return;
        }

        if (event.getMember().getRoles().contains(role)) {
            event.reply("Du hast die Rolle `" + role.getName() + "` bereits.").setEphemeral(true).queue();
        } else {
            guild.addRoleToMember(event.getMember(), role).queue();
            event.reply(event.getMember().getAsMention() + " " +
                    "du hast die Rolle `" + role.getName() + "` erhalten.").queue();
        }
    }

    @Override
    public boolean deferReply() {
        return false;
    }
}
