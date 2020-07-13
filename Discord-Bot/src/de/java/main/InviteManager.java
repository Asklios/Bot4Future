package de.java.main;

import java.time.OffsetDateTime;
import java.util.List;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class InviteManager {
	
	
	Guild guild;
	Long guildID;
	String specialInviteCode;
	int inviteCount;
	
	public InviteManager(Guild guild, String specialInviteCode) {
		
		this.guild = guild;
		this.specialInviteCode = specialInviteCode;
		
		guildID = guild.getIdLong();
		
		saveInviteCount();
		
		//System.out.println(inviteCount + " = inviteCount special code");
		
		
	}
	
	private void saveInviteCount() {

		
		List<Invite> invites = guild.retrieveInvites().complete();
		for (int i = 0; i < invites.size(); i++) {
			
			if (invites.get(i).getCode().equals(specialInviteCode)) {
				inviteCount = invites.get(i).getUses();
			}
		}
		
	}
	
	public void checkNewMember(Member member) {
		try {
			List<Invite> invites = guild.retrieveInvites().complete();
			for (int i = 0; i < invites.size(); i++) {
				if (invites.get(i).getCode().equals(specialInviteCode)) {
					
					if(inviteCount < invites.get(i).getUses()) {
						Role role = member.getGuild().getRoleById(GuildDataXmlReadWrite.readSpecialCodeRole(member.getGuild().getIdLong()));
						try {
							if(role != null) {
								member.getGuild().addRoleToMember(member.getIdLong(), role).complete();
								System.out.println(role.getName() + " was given to " + member.getId());
							}
							else {
								System.out.println("memberid = "+ (member.getGuild().getIdLong()));
							}
						}
						catch (HierarchyException e) {
							TextChannel auditChannel = member.getGuild().getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(member.getGuild().getIdLong()));
							//auditChannel.sendMessage("member.getAsMention()");
							if (auditChannel == null) return; 
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTimestamp(OffsetDateTime.now());
							builder.setColor(0xff0000); // FFF_grün
							builder.setThumbnail(member.getGuild().getSelfMember().getUser().getAvatarUrl() == null ? member.getGuild().getSelfMember().getUser().getDefaultAvatarUrl() : member.getGuild().getSelfMember().getUser().getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
							builder.addField("Name Nutzer*in / ID: ", member.getAsMention() + " / " + member.getId() , false);
							builder.addField("SpecialRole / ID: ", role.getAsMention() + " / " + role.getId(), false);
							builder.addField("Exeption: ", "Die SpecialRole konnte nicht an " + member.getAsMention() + " vergeben werden, da " + role.getAsMention() + " über der höchsten BotRolle steht.", false);
							builder.setTitle(":no_pedestrians: HierarchyException:");
							
							auditChannel.sendMessage(builder.build()).queue();
						}
						System.out.println(member.getUser().getName() + " used special code");
						inviteCount = invites.get(i).getUses();
						GuildDataXmlReadWrite.writeSpecialCodeUser(member);
					}
				}
			}
		}
		catch(InsufficientPermissionException e) {
			//System.err.println("InsufficientPermissionException: bot needs MANAGE_SERVER Permission!");
			//throws Exeption but executes try path afterward????
		}
			
	}
	
	
	public Long getGuildIDofInviteManager() { // guildID ist extern erreichbar
		return guildID;
	}
	
	
	public void setSpecialInviteCode(String newCode) {
		
		specialInviteCode = newCode;
		saveInviteCount();
		
	}
	
}
