package de.java.main.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

public class ClearCommand implements ServerCommand{

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		if(member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {// wenn der Nutzer die Berechtigung hat Nachrichten zu löschen
		//	message.delete().queue(); // Nachricht des Nutzers wird gelöscht --> Bot verzählt sich
			String[] messageSplit = message.getContentDisplay().split(" ");
		
					
			// %clear 3
			if(messageSplit.length == 2) {
				try {
					int amount = Integer.parseInt(messageSplit[1]);
					if(amount >= 0) {
						channel.purgeMessages(get(channel, amount, message));
						channel.sendMessage(amount + "Nachricht(en) gelöscht.").complete().delete().queueAfter(5, TimeUnit.SECONDS); // Bestätigung wird geschickt aber nach angegebener Zeit gelöscht
					}
					else {
						channel.sendMessage(">>> Zwei Mathematikprofessoren unterhalten sich vor einem Hörsaal, "
								+ "ohne dass sie in diesen hineinsehen können. Sie beobachten wie fünf Studenten "
								+ "hineingehen und dann sehen sie wie sechs wieder herauskommen. Daraufhin sagt"
								+ " einer der Mathematikprofessoren zu dem anderen:"
								+ " \"Wenn jetzt noch einer reingeht, ist der Saal leer!\"").complete().delete().queueAfter(120, TimeUnit.SECONDS);						
					}
				} catch (NumberFormatException e) {
					//e.printStackTrace(); // Konsolenausgabe Fehlermeldung
					System.err.println("Cought exception: clear amount larger than int (ClearCommand.java)");
				}
			}
			
			else {
				EmbedBuilder builder = new EmbedBuilder();
				channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				builder.setDescription("%clear <Anzahl der zu löschenden Nachrichten>");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}			
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
			builder.setDescription("%clear <Anzahl der zu löschenden Nachrichten>");
			channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
		}
	}

	
	
	public List<Message> get(MessageChannel channel, int amount, Message originMessage) {
		List<Message> messages = new ArrayList<>();
		int i = amount +1;

		for(Message message : channel.getIterableHistory().cache(false)) { // Nachrichten werden durchgegangen
			if(!message.isPinned() && !message.equals(originMessage)) { // angepinnte Nachrichten werden nicht gelöscht, zählt aber zu gelöschten
				messages.add(message);	

			}
			if(--i <= 0) return messages; //gepinnte Nachricht wird nicht gelöscht aber mitgezählt
		}
		
		return messages;
	}
	
}
