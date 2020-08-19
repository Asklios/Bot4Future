package main.java.commands.pnSystem;

import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetPnChannelCommand implements ServerCommand{
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
			
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
		
		
			try {
				long pnChannelId = GuildDataXmlReadWrite.readPnChannelId(member.getGuild().getIdLong());
				if(pnChannelId != 0) {
					//String auditChannelID = channel.getGuild().getGuildChannelById(auditChannelId).getId();
					channel.sendMessage("<#" + pnChannelId + "> ist der aktuelle PN-Channel.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				else {
					
					channel.sendMessage("Der PN-Channel ist noch nicht festgelegt.\n```%pnchannel #channel```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				System.err.println("Cought Exception: NumberFormatException (GetPnChannelCommand.java - performCommand)");
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}				
}