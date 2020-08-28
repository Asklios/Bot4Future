package main.java.commands.administation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.LiteSQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class ReactRolesCommand implements ServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		// %reactionrole #channel <MessageID> <Emote> @Rolle		
		if(member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
		
			String[] messageText = message.getContentDisplay().split("\\s+"); 
			
			if (messageText.length == 5) {
				
				List<TextChannel> channels = message.getMentionedChannels();
				List<Role> roles = message.getMentionedRoles();
				Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);
				
				if (!channels.isEmpty() && !roles.isEmpty()) {
					
					TextChannel tc = channels.get(0);
					Role role = roles.get(0);
					String messageIDString = messageText[2];
					if (highestBotRole.canInteract(role)) {
						try {
							
							long messageID = Long.parseLong(messageIDString);
							
							String emote = messageText[3];
							tc.addReactionById(messageID, emote).queue();
							
							LiteSQL.onUpdate("INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES(" + 
							channel.getGuild().getIdLong() + ", " + tc.getIdLong() + ", " + messageID + ", '" + emote + "', " + role.getIdLong() + ")");
							
							
						}
						catch (NumberFormatException e) {
							
						}
					}	
					else {
						channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
				}
				else if (channels.isEmpty()) {
					channel.sendMessage("Es wurde kein TextChannel erwähnt \n ```%reactionrole #channel <MessageID> <Emote> @Rolle```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				else {
					channel.sendMessage("Es wurde keine Rolle erwähnt \n ```%reactionrole #channel <MessageID> <Emote> @Rolle```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}	
			}
			else {
				channel.sendMessage("```%reactionrole #channel <MessageID> <Emote> @Rolle```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}	
		} 
		else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
		
	}

	
}
