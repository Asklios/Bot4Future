package de.java.main.commands;

import java.util.concurrent.TimeUnit;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetSpecialCodeCommand implements ServerCommand{

// gibt den aktuell festgelegtn SpecialInviteCode aus	
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
			
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
		
		
			try {
				String inviteCode = GuildDataXmlReadWrite.readSpecialCode(member.getGuild().getIdLong());
				if(!inviteCode.equals("")) {
					channel.sendMessage("\"" + inviteCode + "\" ist der aktuelle Special-Code.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				else {
					channel.sendMessage("Der aktuelle Special-Code ist nicht festgelegt.\n```%specialcode <code>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				System.err.println("Cought Exception: NumberFormatException (SpecialInviteCodeCommand.java - performCommand)");
			}
		}
		else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}	
}


