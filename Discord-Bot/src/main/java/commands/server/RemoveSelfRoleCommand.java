package main.java.commands.server;

import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoveSelfRoleCommand implements ServerCommand{

    SelfRoles selfRoles = new SelfRolesSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        //%rmselfrole <roleName>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        if (!message.getMentionedRoles().isEmpty()) {
            Role role = message.getMentionedRoles().get(0);

            sendAuditMessage(role, member);

            selfRoles.removeSelfRoleByRoleId(message.getGuild().getIdLong(), role.getIdLong());

            channel.sendMessage("`" + role.getName() + "` wird von den selbstgebbaren Rollen entfernt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }
        else {
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

            if (role != null) {
                sendAuditMessage(role, member);

                selfRoles.removeSelfRoleByRoleId(message.getGuild().getIdLong(), role.getIdLong());

                channel.sendMessage("`" + role.getName() + "` wird von den selbstgebbaren Rollen entfernt.")
                        .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
            else {
                channel.sendMessage("Es konnte keine Rolle mit dem Namen \"" + searchString + "\" gefunden werden. " +
                        "Versuche es alternativ mit einer Erwähnung und beachte Groß- und kleinschreibung.")
                        .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        }
    }

    private void sendAuditMessage(Role role, Member member) {
        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0xff00ff); //pink
        b.setTitle(":cricket: SelfRole entfernt");
        b.setTimestamp(OffsetDateTime.now());
        b.setDescription(member.getAsMention() + " hat die Rolle " + role.getName() + "(" + role.getId() + ") " +
                "von den selbstgebbaren Rollen entfernt.");

        audit.sendMessage(b.build()).queue();
    }
}
