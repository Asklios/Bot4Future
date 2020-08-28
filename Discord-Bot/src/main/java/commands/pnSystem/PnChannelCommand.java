package main.java.commands.pnSystem;

import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PnChannelCommand implements ServerCommand{

Long guildID;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
	
		guildID = channel.getGuild().getIdLong();
		
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
			
			String[] messageSplit = message.getContentDisplay().split("\\s+");
			
					
			// %pnchannel #botpn
			
			if(messageSplit.length == 2) {
				
				try {
					
					try {
						GuildDataXmlReadWrite.writePnChannel(message.getMentionedChannels().get(0).getIdLong(), guildID);
						channel.sendMessage("Die PN-Nachrichten werden jetzt in " + message.getMentionedChannels().get(0).getAsMention() + " gesendet.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}					
					catch (IndexOutOfBoundsException e) {
						// System.err.println("Cought Exception: IndexOutOfBoundsException (AuditChannelCommand.java - performCommand)");
						channel.sendMessage("Textchannel nicht gefunden.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
					}
									
				} catch (NumberFormatException e) {
					System.err.println("Cought Exception: NumberFormatException (PnChannelCommand.java - performCommand)");
				}
			} else {
				
				EmbedBuilder builder = new EmbedBuilder();
				channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				builder.setDescription("%pnchannel #channel");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
}
