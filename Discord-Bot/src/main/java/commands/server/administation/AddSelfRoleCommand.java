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

        Guild guild = channel.getGuild();

        if (!message.getMentionedRoles().isEmpty()) {
            List<Role> roles = message.getMentionedRoles();
            roles.forEach(r -> {
                selfRoles.addSelfRole(guild.getIdLong(), r.getName(), r.getIdLong());
                channel.sendMessage("`" + r.getName() + "` wurde zu den selbst gebbaren Rollen hinzugefügt.")
                        .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
                sendAuditMessage(r, member);
            });
            return;
        } else sendFormat(message);

    }

    private void sendFormat(Message msg) {
        msg.reply(new EmbedBuilder().setTitle("Falsches Format")
                .setDescription("````\n%selfrole @role1 @role2 @roleN\n```")
                .build()).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
    }

    private void sendAuditMessage(Role role, Member member) {
        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());

        if (audit == null) return;

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0xff00ff); //pink
        b.setTitle(":woman_mage: Neue SelfRole");
        b.setTimestamp(OffsetDateTime.now());
        b.setDescription(member.getAsMention() + " hat die Rolle " + role.getName() + "(" + role.getId() + ") " +
                "zu den selbstgebbaren Rollen hinzugefügt");

        audit.sendMessage(b.build()).queue();
    }
}
