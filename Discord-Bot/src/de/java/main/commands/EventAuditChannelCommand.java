package de.java.main.commands;

/*import java.util.concurrent.TimeUnit;

import de.java.main.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;*/
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class EventAuditChannelCommand implements ServerCommand {

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		// nur damit kein fehler kommt, kann wieder gslöscht werden
		
	}
	/*
	Long guildID;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
	
		guildID = channel.getGuild().getIdLong();
		
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {// wenn der Nutzer die "Admin" Berechtigung hat
			
			String[] messageSplit = message.getContentDisplay().split(" ");
			
					
			// %auditchannel #audit
			
			if(messageSplit.length == 2) {
				
				try {
					
					try {
						GuildDataXmlReadWrite.writeEventAuditChannel(message.getMentionedChannels().get(0).getIdLong(), guildID);
						channel.sendMessage("Das Event-Audit wird jetzt in " + message.getMentionedChannels().get(0).getAsMention() + " gesendet.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}					
					catch (IndexOutOfBoundsException e) {
						// System.err.println("Cought Exception: IndexOutOfBoundsException (AuditChannelCommand.java - performCommand)");
						channel.sendMessage("Textchannel nicht gefunden.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
									
				} catch (NumberFormatException e) {
					System.err.println("Cought Exception: NumberFormatException (EventAuditChannelCommand.java - performCommand)");
				}
			} else {
				EmbedBuilder builder = new EmbedBuilder();
				channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				builder.setDescription("%eventaudit #channel");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}*/
}
