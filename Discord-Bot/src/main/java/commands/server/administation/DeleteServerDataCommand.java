package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.*;
import main.java.files.interfaces.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.TimeUnit;

public class DeleteServerDataCommand implements ServerCommand {

    CallDatabase callDatabase = new CallDatabaseSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    InviteDatabase inviteDatabase = new InviteDatabaseSQLite();
    RoleDatabase roleDatabase = new RoleDatabaseSQLite();
    UnbanHandlerDatabase unbanHandlerDatabase = new UnbanHandlerDatabaseSQLite();
    UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();
    VoteDatabase voteDatabase = new VoteDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        //%yesiwanttodeleteallserverdata
        long guildId = channel.getGuild().getIdLong();
        channel.sendMessage("**Starte Löschung der Daten...**").queue(m -> {
            callDatabase.removeGuildData(guildId);
            m.editMessage("*deleted callData*").queue();
            channelDatabase.removeGuildData(guildId);
            m.editMessage("*deleted channelData*").queue();
            inviteDatabase.deleteGuildData(guildId);
            m.editMessage("*deleted inviteData*").queue();
            roleDatabase.removeEntriesByGuildId(guildId);
            m.editMessage("*deleted roleData*").queue();
            unbanHandlerDatabase.removeGuildData(guildId);
            m.editMessage("*deleted unbanData*").queue();
            userRecordsDatabase.removeGuildData(guildId);
            m.editMessage("*deleted userRecords*").queue();
            voteDatabase.removeGuildData(guildId);
            m.editMessage("*deleted voteData*").queue();
            m.editMessage("**Es wurden alle Daten gelöscht.**").queue();
        });
    }
}
