package main.java.commands.invite;

import java.util.List;
import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class VerifyCommand implements ServerCommand {

		
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		long specialCodeRoleId = GuildDataXmlReadWrite.readSpecialCodeRole(member.getGuild().getIdLong());
		
		try {
			if(specialCodeRoleId != 0) {
				
				if(hasRole(member, specialCodeRoleId)) {
					giveVerifiableRole(member, channel, message);
				}
			}
			else {
				channel.sendMessage("Die aktuelle Special-Code-Rolle ist nicht festgelegt.\n```%specialcoderole @role```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
		}
		catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.err.println("Cought Exception: NumberFormatException (VerifyCommand.java - performCommand)");
		}
	}
	
	private void giveVerifiableRole (Member member, TextChannel channel, Message message) {
		try {
			long verifiableRoleId = GuildDataXmlReadWrite.readVerifiableRole(member.getGuild().getIdLong());
			
			if(verifiableRoleId != 0) {
				
				List<Member> members = message.getMentionedMembers();
				
				for (Member m : members) {
					Guild guild = m.getGuild();
					channel.sendTyping().queue();
										
					if(!hasRole(m, verifiableRoleId)) {
						guild.addRoleToMember(m.getIdLong(), guild.getRoleById(verifiableRoleId)).queue();
						GuildDataXmlReadWrite.writeVerifiedUser(member, m);
						channel.sendMessage( m.getAsMention() + " hat die Rolle " + guild.getRoleById(verifiableRoleId).getAsMention() + " erhalten.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					} else {
						channel.sendMessage(m.getAsMention() + " hat die Rolle " + guild.getRoleById(verifiableRoleId).getAsMention() + " schon.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
				}
				
			}
			else {
				channel.sendMessage("Die aktuelle VerifiableRole ist nicht festgelegt.\n```%verifiablerole @role```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
			
		} 
		catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.err.println("Cought Exception: NumberFormatException (SpecialInviteCodeCommand.java - performCommand)");
		}
	}
	
	private boolean hasRole(Member member, long roleId) {
		List<Role> memberRoles = member.getRoles();
		
		for (Role r : memberRoles) { // überprüfen ob der Nutzer die specialCodeRole besitzt
			if (r.getIdLong() == roleId) {
				return true;
			}
		}
		return false;
	}
}


