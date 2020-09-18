package main.java.listener;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

/*import java.time.OffsetDateTime;
import java.util.function.Consumer;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;*/

public class EventAuditListener extends ListenerAdapter {

    //Event wenn eine Nachricht bearbeitet wird
	/*@Override
	public void onGuildMessageUpdate (GuildMessageUpdateEvent event) {
		
		Guild guild = event.getGuild();
		User targetUser = event.getAuthor();
		String newMessage = event.getMessage().getContentRaw();
		
		System.out.println(newMessage);
		guild.getTextChannelById(event.getChannel().getIdLong()).retrieveMessageById(event.getMessageIdLong()).queue(message -> {System.out.println(message.getContentRaw());});
		
		
		
		//outputMessageUpdateMessage(guild.getTextChannelById(GuildDataXmlReadWrite.readEventAuditChannelId(guild.getIdLong())), targetUser);
		
	}*/

    //Event wenn eine Nachricht gelöscht wird
	
	/*
	//Event wenn ein Nutzer beitritt
	@Override
	public void onGuildMemberJoin (GuildMemberJoinEvent event) {
		
		Guild guild = event.getGuild();
		User targetUser = event.getUser();
		
		outputMemberJoinMessage(guild.getTextChannelById(GuildDataXmlReadWrite.readEventAuditChannelId(guild.getIdLong())), targetUser);
	}*/

    //Event wenn ein Nutzer den Server verlässt
	/*@Override
	public void onGuildMemberLeave (GuildMemberLeaveEvent event) {
		
		Guild guild = event.getGuild();
		User targetUser = event.getUser();
		
		outputMemberLeaveMessage(guild.getTextChannelById(GuildDataXmlReadWrite.readEventAuditChannelId(guild.getIdLong())), targetUser);
	}*/
	
	/*private void handleAudit(Guild guild,ActionType actionType, Consumer<AuditLogEntry> onFind) {
		
		AuditLogPaginationAction logs = guild.retrieveAuditLogs();
		logs.type(actionType);
		logs.limit(1);
		logs.queue(entries -> {
		    if(entries.size() == 0) return;
		    AuditLogEntry entry = entries.get(0);
		    
		    onFind.accept(entry); //Consumer kann jederzeit aufgerufen, eigenes queue
		    
		    
		});
	}*/
	
	/*private void outputMessageUpdateMessage(TextChannel channel, User targetUser) {
		// Nachricht wird in den festgelegten auditChannel gesendet
		if (channel == null) return; 
				
		
		EmbedBuilder builder = new EmbedBuilder();
		// Inhalt der Auditausgabe Bearbeitung einer Nachricht
		
		;
		builder.setTitle(":pencil: Nachricht bearbeitet");
		builder.setTimestamp(OffsetDateTime.now());
		builder.setColor(0xffbb33); // gelb/orange
		builder.setFooter(targetUser.getName(), targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl());
		builder.addField("Name/ID: ", targetUser.getAsMention() + " / " + targetUser.getId().toString() , false);
		
				
		channel.sendMessage(builder.build()).queue();
				
	}*/
	
	/*
	private void outputMemberJoinMessage(TextChannel channel, User targetUser) {
		if (channel == null) return;
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(":wave: Nutzer ist dem Server beigetreten");
		builder.setTimestamp(OffsetDateTime.now());
		builder.setColor(0x99cc00); //olivgrün
		
		builder.addField("Nickname: ", targetUser.getAsMention() , true);
		builder.addField("ID: ", targetUser.getId(), true);
		builder.setImage(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl());
		
		channel.sendMessage(builder.build()).queue();
	}*/
	
	/*private void outputMemberLeaveMessage(TextChannel channel, User targetUser) {
		if (channel == null) return;
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(":person_running: Nutzer hat den Server verlassen");
		builder.setTimestamp(OffsetDateTime.now());
		builder.setColor(0x99cc00); //olivgrün
		
		builder.addField("Nickname: ", targetUser.getAsMention() , true);
		builder.addField("ID: ", targetUser.getId(), true);
		builder.setImage(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl());
		
		channel.sendMessage(builder.build()).queue();
	}*/
} 
