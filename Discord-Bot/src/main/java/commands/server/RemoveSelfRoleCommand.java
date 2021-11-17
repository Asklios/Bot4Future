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

public class RemoveSelfRoleCommand implements ServerCommand {

    SelfRoles selfRoles = new SelfRolesSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        if (!message.getMentionedRoles().isEmpty()) {
            Role role = message.getMentionedRoles().get(0);

            sendAuditMessage(role, member);

            selfRoles.removeSelfRoleByRoleId(message.getGuild().getIdLong(), role.getIdLong());

            channel.sendMessage("`" + role.getName() + "` wird von den selbstgebbaren Rollen entfernt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        } else sendFormat(message);

    }

    private void sendFormat(Message msg) {
        msg.reply(new EmbedBuilder().setTitle("Falsches Format")
                .setDescription("```\n%rmselfrole @role1 @role2 @roleN\n```")
                .build()).queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
    }

    private void sendAuditMessage(Role role, Member member) {
        TextChannel audit = channelDatabase.getAuditChannel(role.getGuild());

        if (audit == null) return;
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0xff00ff); //pink
        b.setTitle(":cricket: SelfRole entfernt");
        b.setTimestamp(OffsetDateTime.now());
        b.setDescription(member.getAsMention() + " hat die Rolle " + role.getName() + "(" + role.getId() + ") " +
                "von den selbstgebbaren Rollen entfernt.");

        audit.sendMessage(b.build()).queue();
    }
}
