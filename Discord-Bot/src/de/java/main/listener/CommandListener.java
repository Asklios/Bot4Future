package de.java.main.listener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.java.main.DiscordBot;
import de.java.main.commands.SpecialCodeCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		
		String message = event.getMessage().getContentDisplay(); // Nachricht wie sie ankommt mit Formatierung
		//System.out.println(event.getMessageId());
		//System.out.println(event.getMessageIdLong());
				
				
		
		if(event.isFromType(ChannelType.TEXT)) {
			TextChannel channel = event.getTextChannel();
			
			if(message.startsWith("%")) { // Festlegung des Präfix
				String[] args = message.substring(1).split(" ");
				if(args.length > 0) {
					if(!DiscordBot.INSTANCE.getCmdMan().perform(args[0], event.getMember(), channel, event.getMessage())) {
						channel.sendMessage("unknown command").complete().delete().queueAfter(5, TimeUnit.SECONDS); // wenn if nicht true, dann Fehlermeldung
					}
				}
				event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
			}
			
			else {
				DiscordBot.INSTANCE.getAutoListener().autoListen(event.getMember(), channel, event.getMessage());
			}
			
		}
		
	}
	
	// Invite Manager
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		
		SpecialCodeCommand.guildMemberJoin(event);
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		System.out.println("connected to: " + event.getGuild().getName());
		ArrayList<Guild> singleGuildList = new ArrayList<Guild>();
		singleGuildList.add(event.getGuild());
		
		DiscordBot.INSTANCE.updateGuilds(singleGuildList);
		SpecialCodeCommand.writeInviteCount(singleGuildList);
	}
	
	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("ready");
		
		DiscordBot.INSTANCE.updateGuilds(event.getJDA().getGuilds());
		SpecialCodeCommand.writeInviteCount(event.getJDA().getGuilds());
	}	
}


