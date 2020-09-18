package main.java.commands.invite;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class VerifiableRoleCommand implements ServerCommand {

    GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        try {
            Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

            if (message.getMentionedRoles().isEmpty()) {
                channel.sendMessage("Es wurde keine Rolle erwähnt. \n ```%verifiablerole @Rolle```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
                return;
            }

            if (!message.getMentionedRoles().get(0).isManaged() && highestBotRole.canInteract(message.getMentionedRoles().get(0))) {

                this.guildDatabase.setVerifyRole(channel.getGuild(), message.getMentionedRoles().get(0));
                channel.sendMessage("VerifiableRole wurde auf " + message.getMentionedRoles().get(0).getAsMention() + " gesetzt").complete().delete().queueAfter(10, TimeUnit.SECONDS);

            } else if (!highestBotRole.canInteract(message.getMentionedRoles().get(0))) {
                channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            } else if (message.getMentionedRoles().get(0).isManaged()) {
                channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie von einer Integration verwaltet wird.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
