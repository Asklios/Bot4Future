package main.java.commands.administation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class LogCommand implements ServerCommand{

	List<String> messages;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		if (member.hasPermission(channel, Permission.ADMINISTRATOR)) {
			
			String[] messageSplit = message.getContentDisplay().split(" ");
			
			//%log 15
			if(messageSplit.length == 2) {
				try {
					int amount = Integer.parseInt(messageSplit[1]);
					if(amount > 0) {
						if(amount <= 100) {
							channel.sendTyping().queue();
							writeFile(message, channel, amount);
						}
						else {
							channel.sendMessage("Sagmal soll ich jetzt Romane schreiben?!?! ```Nachrichtenlimit: 100```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
						}
					}
					else {
						channel.sendMessage("Stell Dir vor, Du hast null Kekse und verteilst sie gleichmäßig an null Freunde. Siehst Du? Das macht keinen Sinn. Und das Krümelmonster ist traurig, weil keine Kekse mehr da sind und Du bist traurig, weil Du keine Freunde hast.").complete().delete().queueAfter(25, TimeUnit.SECONDS);			
					}
				}
				catch (NumberFormatException e) {
					System.err.println("Cought exception: log amount larger than int (LogCommand.java)");
				}
				catch (ArrayIndexOutOfBoundsException e) {
					channel.sendMessage("Falsche Formatierung: ```%log <Anzahl>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
			}
			else {
				channel.sendMessage("Zukunftsvorhersage in 5, 4, 3, $%*, $%& - **critical ERROR occurred**").complete().delete().queueAfter(12, TimeUnit.SECONDS);
			}
		}
		else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
		
	}
	
	//Nachrichten werden abgerufen und in eine .txt geschrieben
	private void writeFile(Message originMessage, MessageChannel channel, int amount) {
		
		String path = DiscordBot.INSTANCE.getLogFilePath();
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		
		MessageHistory messageHistory = channel.getHistoryBefore(originMessage, amount).complete();
		List<Message> messageList = messageHistory.getRetrievedHistory();
		List<String> textList = new ArrayList<>();
		textList.add("Logfile - time created: " + OffsetDateTime.now().format(dateFormat) + " - guild: " + originMessage.getGuild().getName() + " - channel: #" + originMessage.getChannel().getName() + " - requested by: " + originMessage.getAuthor().getName());
		textList.add("");
		
		String messageTime = "time";
		String author = "author";
		String messageText = "text";
			
		for (Message message : messageList) {
			messageTime = message.getTimeCreated().format(dateFormat).toString();
			author = "(" + message.getAuthor().getId() + ")" + message.getAuthor().getName().toString();
			messageText = message.getContentRaw();
			
			textList.add(messageTime + " - by: " + String.format("%-"+50+"."+50+"s" , author) + " - message: " + messageText);
		}
		
		textList.add("");
		textList.add("-- Logfile created by Bot4Future --");
		
		try {
			OutputStreamWriter myWriter = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
			
			String text = String.join("\r\n", textList);

			myWriter.write(text);
			myWriter.close();
			
		}
		catch (IOException | NullPointerException e) {
			e.printStackTrace();
		}
		
		// .txt wird in den Audit-Channel geschickt
		
		TextChannel audit = originMessage.getGuild().getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(originMessage.getGuild().getIdLong()));
		
		EmbedBuilder embed = new EmbedBuilder();
		File file = new File(DiscordBot.INSTANCE.getLogFilePath());
		String fileName = "log " + OffsetDateTime.now().format(dateFormat) + " " + channel.getName() + ".txt";
		try {
			embed.setColor(0xcc880); //orange-braun
			embed.setTitle(":scroll: Logfile created");
			embed.setAuthor(originMessage.getGuild().getSelfMember().getNickname());
			embed.setTimestamp(OffsetDateTime.now());
			embed.setThumbnail(originMessage.getGuild().getSelfMember().getUser().getAvatarUrl());
			embed.setDescription("Log - requested by " + originMessage.getAuthor().getAsMention() + "\n enthält die letzten " + amount + " Nachrichten aus <#" + channel.getId() + ">");
			audit.sendFile(file, fileName).embed(embed.build()).complete();
		}
		catch (IllegalArgumentException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
		catch (InsufficientPermissionException e) {
			channel.sendMessage("Dem Bot fehlt die Berechtigung das LogFile zu senden. ```Benötigt Berechtigungen: message_read, message_write, message_attach_files```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
		
		// .txt wird gelöscht
		file.delete();
		
	}
}
