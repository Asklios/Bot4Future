package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MuteRoleCommand implements ServerCommand {

    private RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        if (message.getMentionedRoles().isEmpty()) {
            channel.sendMessage("Es wurde keine Rolle erwähnt. `%muterole @role`")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Guild guild = message.getGuild();
        Role newMuterole = message.getMentionedRoles().get(0);

        Role oldMuteRoleId = this.roleDatabase.getMuteRole(guild);

        if (oldMuteRoleId != null) {
            List<Member> mutedMembers = guild.getMembersWithRoles(oldMuteRoleId);
            mutedMembers.forEach(m -> guild.addRoleToMember(m, newMuterole).queue());
        }

        this.roleDatabase.setMuteRole(guild, newMuterole);

        channel.sendMessage("Die Muterolle wurde auf " + newMuterole.getName() + " (" + newMuterole.getId() + ") festgelegt.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
