package de.java.main.commands;

import java.util.concurrent.TimeUnit;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetSpecialCodeRoleCommand implements ServerCommand{

	// gibt die Rolle aus welche bei Beitritt über den special Code vergeben wird.	
		
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
		
			try {
				
				long specialCodeRoleId = GuildDataXmlReadWrite.readSpecialCodeRole(member.getGuild().getIdLong());
				if(specialCodeRoleId != 0) {
					String roleMention = message.getGuild().getRoleById(specialCodeRoleId).getAsMention();
					channel.sendMessage(roleMention + " ist die aktuelle Special-Code-Rolle.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				} else {
					channel.sendMessage("Die aktuelle Special-Code-Rolle ist nicht festgelegt.\n```%specialcoderole @role```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				System.err.println("Cought Exception: NumberFormatException (SpecialInviteCodeCommand.java - performCommand)");
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}	
}
