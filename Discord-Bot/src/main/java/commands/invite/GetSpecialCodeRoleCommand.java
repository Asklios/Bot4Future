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

public class GetSpecialCodeRoleCommand implements ServerCommand {

    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            channel.sendMessage("Du benötigst die Berechtigung Administrator um diesen Command zu nutzen. :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Role specialRole = this.guildDatabase.getSpecialRole(member.getGuild());

        if (specialRole == null) {
            channel.sendMessage("Die special Rolle wurde noch nicht festgelegt `%specialcoderole @role`")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        channel.sendMessage("Die aktuelle specialcoderole ist " + specialRole.getName() + " (" + specialRole.getId() + ")")
                .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
    }
}
