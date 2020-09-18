package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class GetMuteRoleCommand implements ServerCommand {

    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) return;

        Role muteRole = this.guildDatabase.getMuteRole(member.getGuild());

        if (muteRole == null) {
            channel.sendMessage("Die Muterolle wurde noch nicht festgelegt `%muterole @role`").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }

        channel.sendMessage("Die aktuelle Muterolle ist " + muteRole.getAsMention()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
    }
}
