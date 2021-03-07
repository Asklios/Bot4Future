package main.java.commands.server.invite;

import main.java.DiscordBot;
import main.java.GuildData;
import main.java.InviteManager;
import main.java.commands.server.ServerCommand;
import main.java.files.impl.RoleDatabaseSQLite;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpecialCodeCommand implements ServerCommand {

    private static List<InviteManager> inviteManagers = new ArrayList<>();
    RoleDatabase roleDatabase = new RoleDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        // %specialcode <code>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        if (messageSplit.length != 2) {
            channel.sendMessage("Falsche Formatierung! `%specialcode <code>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        try {
            String code = (messageSplit[1]);
            changeSpecialInviteCode(code, member.getGuild());
            channel.sendMessage("SpecialInviteCode wurde auf \"" + code + "\" gesetzt").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS)); // Bestätigung wird geschickt aber nach angegebener Zeit gelöscht

        } catch (NumberFormatException e) {
            System.err.println("Cought Exception: NumberFormatException (SpecialCodeCommand.java - performCommand)");
        }
    }

    private void changeSpecialInviteCode(String newCode, Guild guild) {
        if (inviteManagers.size() <= 0) return;

        for (InviteManager i : inviteManagers) {
            if (i.getGuildIDofInviteManager().equals(guild.getIdLong())) {

                i.setSpecialInviteCode(newCode);
                this.roleDatabase.setSpecialCode(guild, newCode);
            }
        }
    }

    // Invite Manager
    public static void guildMemberJoin(GuildMemberJoinEvent event) {
        if (inviteManagers.size() > 0) {

            for (InviteManager i : inviteManagers) {
                if (i.getGuildIDofInviteManager() == event.getGuild().getIdLong()) {
                    i.checkNewMember(event.getMember());
                }
            }
        }
    }

    public static void writeInviteCount(List<Guild> guilds) {

        List<GuildData> guildsData = DiscordBot.INSTANCE.getGuildsData();

        if (guildsData.size() != guilds.size()) {
            System.err.println("guild.size != guildData.size");
        }
        for (Guild guild : guilds) {

            for (GuildData guildData : guildsData) {
                if (guildData.getID() == guild.getIdLong()) {
                    inviteManagers.add(new InviteManager(guild, guildData.getSpecialInviteCode()));
                    break;
                }
            }
        }
    }
}
	


