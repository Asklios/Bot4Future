package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetMuteRoleCommand implements ServerCommand {

    private RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Role muteRole = this.roleDatabase.getMuteRole(member.getGuild());

        if (muteRole == null) {
            channel.sendMessage("Die Muterolle wurde noch nicht festgelegt `%muterole @role`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        channel.sendMessage("Die aktuelle Muterolle ist " + muteRole.getName() + " (" + muteRole.getId() + ")")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
