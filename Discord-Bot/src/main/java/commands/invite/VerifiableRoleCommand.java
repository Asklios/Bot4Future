package main.java.commands.invite;

import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class VerifiableRoleCommand implements ServerCommand {
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
			
			try {
				Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);
				
				if (!message.getMentionedRoles().isEmpty()) {
				
					if (!message.getMentionedRoles().get(0).isManaged() && highestBotRole.canInteract(message.getMentionedRoles().get(0))) {
					
						GuildDataXmlReadWrite.writeVerifiableRole(message.getMentionedRoles().get(0).getIdLong(), message.getGuild().getIdLong());
						channel.sendMessage("VerifiableRole wurde auf " + message.getMentionedRoles().get(0).getAsMention() + " gesetzt").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					
					}
					else if (!highestBotRole.canInteract(message.getMentionedRoles().get(0))) {
						channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
					
					else if (message.getMentionedRoles().get(0).isManaged()) {
						channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie von einer Integration verwaltet wird.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
				}
				else {
					
					channel.sendMessage("Es wurde keine Rolle erwähnt. \n ```%verifiablerole @Rolle```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
}
