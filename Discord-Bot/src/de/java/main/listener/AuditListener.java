package de.java.main.listener;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;


public class AuditListener extends ListenerAdapter{
	
	@Override
	public void onGuildBan(GuildBanEvent event) {
		
		Guild guild = event.getGuild();
		User targetUser = event.getUser();
		
		handleAudit(guild, ActionType.BAN, entry -> {
			outputBanMessage(guild.getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(guild.getIdLong())), targetUser, entry);
		}); 
	}
	
	@Override
	public void onGuildUnban(GuildUnbanEvent event) {

		Guild guild = event.getGuild();
		User targetUser = event.getUser();
		
		handleAudit(guild, ActionType.UNBAN, entry -> {
			outputUnbanMessage(guild.getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(guild.getIdLong())), targetUser, entry);
		}); 
	}

	private void handleAudit(Guild guild,ActionType actionType, Consumer<AuditLogEntry> onFind) {
		
		AuditLogPaginationAction logs = guild.retrieveAuditLogs();
		logs.type(actionType);
		logs.limit(1);
		logs.queue(entries -> {
		    if(entries.size() == 0) return;
		    AuditLogEntry entry = entries.get(0);
		    
		    onFind.accept(entry); //Consumer kann jederzeit aufgerufen, eigenes queue
		    
		    
		});
	}
	


	private void outputBanMessage(TextChannel channel, User targetUser, AuditLogEntry logEntry) {
		// Nachricht wird in den festgelegten auditChannel gesendet
		if (channel == null) return; 
		
		
		
		EmbedBuilder builder = new EmbedBuilder();
		// Inhalt der Auditausgabe bei Ban
		
		//builder.setFooter(bannedBy);
		builder.setTimestamp(OffsetDateTime.now());
		builder.setColor(0x1da64a); // FFF_grün
		builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
		builder.setFooter("by " + logEntry.getUser().getName());
		builder.addField("Name: ", targetUser.getAsMention() , false);
		builder.addField("ID: ", targetUser.getId(), false);
		builder.addField(":page_facing_up:Begründung: ",logEntry.getReason() , false);
		builder.setTitle(":hammer: Nutzer gebannt:");
		
		channel.sendMessage(builder.build()).queue();
		
	}	 
	 
	private void outputUnbanMessage(TextChannel channel, User targetUser, AuditLogEntry logEntry) {
		// Nachricht wird in den festgelegten auditChannel gesendet
		if (channel == null) return; 
		
		
		
		EmbedBuilder builder = new EmbedBuilder();
		// Inhalt der Auditausgabe bei Entbannung
		
		;
		builder.setTimestamp(OffsetDateTime.now());
		builder.setColor(0x1da64a); // FFF_grün
		builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
		builder.setFooter("by " + logEntry.getUser().getName());
		builder.addField("Name: ", targetUser.getAsMention() , false);
		builder.addField("ID: ", targetUser.getId(), false);
		builder.setTitle(":gear: Nutzer entbannt");
				
		
		channel.sendMessage(builder.build()).queue();
				
	}	 
}
