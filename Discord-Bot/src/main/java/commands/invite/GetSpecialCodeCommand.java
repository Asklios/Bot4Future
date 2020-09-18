package main.java.commands.invite;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetSpecialCodeCommand implements ServerCommand {

    GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        String inviteCode = this.guildDatabase.getSpecialCode(channel.getGuild());

        if (inviteCode == null) {
            channel.sendMessage("Der aktuelle Special-Code ist nicht festgelegt.\n```%specialcode <code>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
            return;
        }

        channel.sendMessage("\"" + inviteCode + "\" ist der aktuelle Special-Code.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
    }
}


