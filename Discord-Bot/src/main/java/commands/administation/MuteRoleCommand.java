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

        if (!member.hasPermission(Permission.ADMINISTRATOR)) return;

        if (message.getMentionedRoles().isEmpty()) return;

        Guild guild = message.getGuild();
        Role newMuterole = message.getMentionedRoles().get(0);

        this.guildDatabase.setMuteRole(guild, newMuterole);

        channel.sendMessage("Die Muterolle wurde auf " + newMuterole.getAsMention() + " festgelegt.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
    }
}
