package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AddSelfRoleCommand implements ServerCommand {

    SelfRoles selfRoles = new SelfRolesSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        //%selfrole <roleName>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        String searchString = "";
        for (int i = 1; i < messageSplit.length; i++) {
            if (searchString.equals("")) searchString = messageSplit[i];
            else searchString = searchString + " " + messageSplit[i];
        }

        List<Role> roles = channel.getGuild().getRoles();
        Role role = null;
        for (Role r : roles) {
            if (r.getName().equals(searchString)) {
                role = r;
                break;
            }
        }

        Guild guild = channel.getGuild();

        if (role != null) {
            selfRoles.addSelfRole(guild.getIdLong(), role.getName(), role.getIdLong());
            channel.sendMessage("`" + role.getName() + "` wurde zu den selbst gebbaren Rollen hinzugefügt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            sendAuditMessage(role, member);
        }
        else {
            RoleAction ra = guild.createRole();
            ra.setName(searchString).queue();
            ra.setPermissions(Permission.EMPTY_PERMISSIONS).queue();
            ra.queue(r -> {
                selfRoles.addSelfRole(guild.getIdLong(), r.getName(), r.getIdLong());
                sendAuditMessage(r, member);
            });
            channel.sendMessage("`" + searchString + "` wurde erstellt und zu den selbst gebbaren Rollen hinzugefügt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }

    }

    private void sendAuditMessage(Role role, Member member) {
        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0xff00ff); //pink
        b.setTitle(":woman_mage: Neue SelfRole");
        b.setTimestamp(OffsetDateTime.now());
        b.setDescription(member.getAsMention() + " hat die Rolle " + role.getName() + "(" + role.getId() + ") " +
                "zu den selbstgebbaren Rollen hinzugefügt");

        audit.sendMessage(b.build()).queue();
    }
}
