package main.java.commands.server.invite;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetSpecialCodeCommand implements ServerCommand {

    RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String inviteCode = this.roleDatabase.getSpecialCode(channel.getGuild());

        if (inviteCode == null) {
            channel.sendMessage("Der aktuelle Special-Code ist nicht festgelegt.\n```%specialcode <code>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        channel.sendMessage("\"" + inviteCode + "\" ist der aktuelle Special-Code.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
    }
}


