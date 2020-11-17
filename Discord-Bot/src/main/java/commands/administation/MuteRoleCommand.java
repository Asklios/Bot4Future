package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.TimeUnit;

public class MuteRoleCommand implements ServerCommand {

    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();

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

        this.guildDatabase.setMuteRole(guild, newMuterole);

        channel.sendMessage("Die Muterolle wurde auf " + newMuterole.getName() + " (" + newMuterole.getId() + ") festgelegt.")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
