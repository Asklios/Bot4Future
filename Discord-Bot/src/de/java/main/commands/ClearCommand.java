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
		
		if(member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {// wenn der Nutzer die Berechtigung hat Nachrichten zu l�schen
		//	message.delete().queue(); // Nachricht des Nutzers wird gel�scht --> Bot verz�hlt sich
			String[] messageSplit = message.getContentDisplay().split(" ");
		
					
			// %clear 3
			if(messageSplit.length == 2) {
				try {
					int amount = Integer.parseInt(messageSplit[1]);
					if(amount >= 0) {
						channel.purgeMessages(get(channel, amount, message));
						channel.sendMessage(amount + "Nachricht(en) gel�scht.").complete().delete().queueAfter(5, TimeUnit.SECONDS); // Best�tigung wird geschickt aber nach angegebener Zeit gel�scht
					}
					else {
						channel.sendMessage(">>> Zwei Mathematikprofessoren unterhalten sich vor einem H�rsaal, "
								+ "ohne dass sie in diesen hineinsehen k�nnen. Sie beobachten wie f�nf Studenten "
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
				builder.setDescription("%clear <Anzahl der zu l�schenden Nachrichten>");
				channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
			}			
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
			builder.setDescription("%clear <Anzahl der zu l�schenden Nachrichten>");
			channel.sendMessage(builder.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
		}
	}

	
	
	public List<Message> get(MessageChannel channel, int amount, Message originMessage) {
		List<Message> messages = new ArrayList<>();
		int i = amount +1;

		for(Message message : channel.getIterableHistory().cache(false)) { // Nachrichten werden durchgegangen
			if(!message.isPinned() && !message.equals(originMessage)) { // angepinnte Nachrichten werden nicht gel�scht, z�hlt aber zu gel�schten
				messages.add(message);	

			}
			if(--i <= 0) return messages; //gepinnte Nachricht wird nicht gel�scht aber mitgez�hlt
		}
		
		return messages;
	}
	
}
