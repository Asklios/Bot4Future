package main.java.commands.server.invite;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class SpecialCodeRoleCommand implements ServerCommand {

    RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String[] messageSplit = message.getContentDisplay().split("\\s+");

        // %specialcoderole @Rolle
        if (messageSplit.length != 2) {
            channel.sendMessage("Falsche Formatierung!").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            EmbedBuilder builder = new EmbedBuilder();
            builder.setDescription("%specialrole @role");
            channel.sendMessage(builder.build()).queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        try {
            Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

            if (message.getMentionedRoles().isEmpty()) {
                channel.sendMessage("Es wurde keine Rolle erwähnt. \n ```%specialcoderole @Rolle```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                return;
            }

            if (!message.getMentionedRoles().get(0).isManaged() && highestBotRole.canInteract(message.getMentionedRoles().get(0))) {

                this.roleDatabase.setSpecialRole(channel.getGuild(), message.getMentionedRoles().get(0));
                channel.sendMessage("SpecialCodeRole wurde auf " + message.getMentionedRoles().get(0).getAsMention() + " gesetzt").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));

            } else if (!highestBotRole.canInteract(message.getMentionedRoles().get(0))) {
                channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            } else if (message.getMentionedRoles().get(0).isManaged()) {
                channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie von einer Integration verwaltet wird.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
