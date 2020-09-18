package main.java.commands.audit;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetEventAuditChannelCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        // nur damit kein fehler kommt, kann wieder gelöscht werden

    }

	/*
	// gibt den aktuell festgelegten Event-Audit-Channel aus	
		
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
			
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
		
		
			try {
				long eventAuditChannelId = GuildDataXmlReadWrite.readEventAuditChannelId(member.getGuild().getIdLong());
				if(eventAuditChannelId != 0) {
					System.out.println(eventAuditChannelId);
					channel.sendMessage("<#" + eventAuditChannelId + "> ist der aktuelle Event-Audit-Channel.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				else {
					
					channel.sendMessage("Der Event-Auditchannel ist noch nicht festgelegt.\n```%eventaudit #channel```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				System.err.println("Cought Exception: NumberFormatException (GetEventAuditChannelCommand.java - performCommand)");
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}*/
}