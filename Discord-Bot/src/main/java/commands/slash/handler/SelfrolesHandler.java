package main.java.commands.slash.handler;

import main.java.commands.server.user.IAmCommand;
import main.java.commands.slash.SlashCommandHandler;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class SelfrolesHandler implements SlashCommandHandler {


    SelfRoles selfRoles = new SelfRolesSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void handle(SlashCommandEvent event) {
        if (event.getGuild().getMember(event.getUser()).hasPermission(Permission.ADMINISTRATOR)) {
            switch (event.getSubcommandName()) {
                case "add": {
                    handleAdd(event);
                    break;
                }
                case "remove": {
                    handleRemove(event);
                    break;
                }
            }
        } else {
            event.reply("Nur Administratoren dürfen die Selbstgebbaren Rollen verwalten!")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private void handleRemove(SlashCommandEvent event) {
        Map<Long, HashMap<String, Long>> selfRoles = IAmCommand.getServerSelfRoles();
        Role role = event.getOption("rolle").getAsRole();

        if (selfRoles.get(event.getGuild().getIdLong()) == null || !selfRoles
                .get(event.getGuild().getIdLong())
                .containsValue(role.getIdLong())) {
            event.reply("Die Rolle " + role.getAsMention() + " ist keine selbst gebbare Rolle!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());
        if (audit != null) {
            EmbedBuilder b = new EmbedBuilder();
            b.setColor(0xff00ff); //pink
            b.setTitle(":cricket: SelfRole entfernt");
            b.setTimestamp(OffsetDateTime.now());
            b.setDescription(event.getMember().getAsMention() + " hat die Rolle "
                    + role.getName() + "(" + role.getId() + ") " +
                    "von den selbstgebbaren Rollen entfernt.");

            audit.sendMessage(b.build()).queue();
        }

        this.selfRoles.removeSelfRoleByRoleId(event.getGuild().getIdLong(), role.getIdLong());

        event.reply(role.getAsMention() + " wurde von den selbstgebbaren Rollen entfernt.")
                .setEphemeral(true)
                .queue();

    }

    private void handleAdd(SlashCommandEvent event) {
        Map<Long, HashMap<String, Long>> selfRoles = IAmCommand.getServerSelfRoles();
        Role role = event.getOption("rolle").getAsRole();
        Guild guild = event.getGuild();

        if (selfRoles.get(event.getGuild().getIdLong()) != null && selfRoles
                .get(event.getGuild().getIdLong())
                .containsValue(role.getIdLong())) {
            event.reply("Die Rolle " + role.getAsMention() + " ist bereits eine selbst gebbare Rolle!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        this.selfRoles.addSelfRole(guild.getIdLong(), role.getName(), role.getIdLong());
        event.reply(role.getAsMention() + " wurde zu den selbst gebbaren Rollen hinzugefügt.")
                .setEphemeral(true)
                .queue();

        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());
        if (audit == null) return;
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0xff00ff); //pink
        b.setTitle(":woman_mage: Neue SelfRole");
        b.setTimestamp(OffsetDateTime.now());
        b.setDescription(event.getMember().getAsMention() + " hat die Rolle " + role.getName()
                + "(" + role.getId() + ") " +
                "zu den selbstgebbaren Rollen hinzugefügt");

        audit.sendMessage(b.build()).queue();


    }
}
