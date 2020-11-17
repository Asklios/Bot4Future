package main.java.commands.invite;

import main.java.DiscordBot;
import main.java.GuildData;
import main.java.InviteManager;
import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
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

    private static List<InviteManager> inviteManagers = new ArrayList<InviteManager>();
    GuildDatabase guildDatabase = new GuildDatabaseSQLite();

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
        if (inviteManagers.size() > 0) {

            for (int i = 0; i < inviteManagers.size(); i++) {
                if (inviteManagers.get(i).getGuildIDofInviteManager().equals(guild.getIdLong())) {

                    inviteManagers.get(i).setSpecialInviteCode(newCode);
                    this.guildDatabase.setSpecialCode(guild, newCode);

                }
            }
        }
    }

    // Invite Manager
    public static void guildMemberJoin(GuildMemberJoinEvent event) {
        if (inviteManagers.size() > 0) {

            for (int i = 0; i < inviteManagers.size(); i++) {
                if (inviteManagers.get(i).getGuildIDofInviteManager() == event.getGuild().getIdLong()) {
                    inviteManagers.get(i).checkNewMember(event.getMember());
                }
            }
        }
    }

    public static void writeInviteCount(List<Guild> guilds) {

        List<GuildData> guildsData = DiscordBot.INSTANCE.getGuildsData();

        if (guildsData.size() != guilds.size()) {
            System.err.println("guild.size != guildData.size");
        }
        for (int i = 0; i < guilds.size(); i++) {

            for (int j = 0; j < guildsData.size(); j++) {
                boolean doBreak = false;
                if (guildsData.get(j).getID() == guilds.get(i).getIdLong()) {
                    inviteManagers.add(new InviteManager(guilds.get(i), guildsData.get(j).getSpecialInviteCode()));
                    doBreak = true;
                    break;
                }
                if (doBreak) {
                    break;
                }
            }
        }
    }
}
	


