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

public class GetVerifiableRoleCommand implements ServerCommand {

    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Role verifiableRole = this.guildDatabase.getVerifyRole(member.getGuild());

        if (verifiableRole == null) {
            channel.sendMessage("Die verifiable Rolle wurde noch nicht festgelegt `%verifiablerole @role`")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        channel.sendMessage("Die aktuelle verifiablerole ist " + verifiableRole.getName() + " (" + verifiableRole.getId() + ")")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
