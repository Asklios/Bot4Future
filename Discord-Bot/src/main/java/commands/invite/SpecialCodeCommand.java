package main.java.commands.invite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.java.DiscordBot;
import main.java.GuildData;
import main.java.InviteManager;
import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class SpecialCodeCommand implements ServerCommand {
	
	private static List<InviteManager> inviteManagers = new ArrayList<InviteManager>();
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {// wenn der Nutzer die "Admin" Berechtigung hat
		
			String[] messageSplit = message.getContentDisplay().split(" ");
		
					
			// %specialcode 4fdaKxy
			if(messageSplit.length == 2) {
				
				try {
					String code = (messageSplit[1]);
					// channel.purgeMessages(get(channel, amount));
					
					changeSpecialInviteCode(code, member.getGuild().getIdLong());
					channel.sendMessage("SpecialInviteCode wurde auf \"" + code + "\" gesetzt").complete().delete().queueAfter(10, TimeUnit.SECONDS); // Bestätigung wird geschickt aber nach angegebener Zeit gelöscht
					
				} catch (NumberFormatException e) {
					System.err.println("Cought Exception: NumberFormatException (SpecialCodeCommand.java - performCommand)");
				}
			} else {
				
				channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				EmbedBuilder builder = new EmbedBuilder();
				builder.setDescription("%specialcode <code>");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
	
	private void changeSpecialInviteCode(String newCode, Long guildID) {
		if (inviteManagers.size() > 0) {
			
			for(int i = 0; i < inviteManagers.size(); i++) {
				if (inviteManagers.get(i).getGuildIDofInviteManager().equals(guildID)) {
					
					inviteManagers.get(i).setSpecialInviteCode(newCode);
					GuildDataXmlReadWrite.writeSpecialCode(newCode, guildID);
					
				}
			}
		}
		
	}
	
	
	
	// Invite Manager
		
	public static void guildMemberJoin(GuildMemberJoinEvent event) {
		if (inviteManagers.size() > 0) {
			
			for(int i = 0; i < inviteManagers.size(); i++) {
				if (inviteManagers.get(i).getGuildIDofInviteManager() == event.getGuild().getIdLong()) {
					
					inviteManagers.get(i).checkNewMember(event.getMember());
					
				}
			}
		}
	}
	
	public static void writeInviteCount(List<Guild> guilds) {
		
		//inviteManagers.clear();
		
		List <GuildData> guildsData = DiscordBot.INSTANCE.getGuildsData();
		
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
				if(doBreak) {
					break;
				}
			}	
		}
	}	
}
	


