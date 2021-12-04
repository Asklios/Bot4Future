package main.java.commands.server.bump;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.TimeUnit;

public class GetBumpRoleCommand implements ServerCommand {

    private RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Role bumpRole = this.roleDatabase.getBumpRole(member.getGuild());

        if (bumpRole == null) {
            channel.sendMessage("Die BumpRolle wurde noch nicht festgelegt `%bumprole @role`")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        channel.sendMessage("Die aktuelle verifiablerole ist `" + bumpRole.getName() + " (" + bumpRole.getId() + ")`")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
