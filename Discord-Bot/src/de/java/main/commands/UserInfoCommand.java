package de.java.main.commands;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class UserInfoCommand implements ServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		if(member.hasPermission(channel, Permission.KICK_MEMBERS)) {
		
			channel.sendTyping().queue();
			List<Member> mention = message.getMentionedMembers();
			if(message.getContentDisplay().split(" ").length > 1 && mention.size() > 0) {
				for(Member user : mention) {
					onInfo(member, user, channel);
				}
			} else {
				EmbedBuilder builder = new EmbedBuilder();
				channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				builder.setDescription("%userinfo @User1 (@User2) (@User3) (...)");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}


	
	private void onInfo(Member requester, Member user, TextChannel channel) {
		
		
		OffsetDateTime usercreated = user.getTimeCreated();
		OffsetDateTime userjoined =user.getTimeJoined();
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		
		String formatUserJoined = userjoined.format(dateFormat);
		String formatUserCreated = usercreated.format(dateFormat);
			
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setFooter("Requested by " + requester.getGuild().getMemberById(requester.getIdLong()).getEffectiveName());
		builder.setColor(0x1da64a);
		builder.setTimestamp(OffsetDateTime.now());
		builder.setThumbnail(user.getUser().getEffectiveAvatarUrl());
		
		StringBuilder strBuilder = new StringBuilder();
		
		strBuilder.append("**User:** " + user.getAsMention() + "\n");
		strBuilder.append("**ClientID:** " + user.getId() + "\n");
		strBuilder.append("\n");
		strBuilder.append("**TimeJoined:** "+ formatUserJoined + "\n");
		strBuilder.append("**TimeCreated:** "+ formatUserCreated + "\n");
		
		strBuilder.append(" \n *Rollen:* \n");
		
		StringBuilder roleBuilder = new StringBuilder();
		for(Role role : user.getRoles()) {
			roleBuilder.append(role.getAsMention() + " ");
		}
		strBuilder.append(roleBuilder.toString().trim() + "\n");
		
		builder.setDescription(strBuilder);
		
		channel.sendMessage(builder.build()).complete().delete().queueAfter(60, TimeUnit.SECONDS);
		
	}
}

