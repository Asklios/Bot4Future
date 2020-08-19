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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class ReportCommand implements ServerCommand {

	List<String> messages;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		String[] messageSplit = message.getContentDisplay().split(" ", 3);
		
		//%report @Nutzer <reason>
		if(messageSplit.length > 2) {
			
			List<Member> members = message.getMentionedMembers();
			Member reportedMember = members.get(0);
			User reportingUser = message.getAuthor();
			String reason = messageSplit[2];
			
			if (!reportedMember.equals(null) && !reason.equals(null)) {
			
				try {
					int amount = 100;
					channel.sendMessage(reportingUser.getAsMention() + " Die verantwortlichen Moderatoren wurden benachrichtigt, es sind keine weiteren Pings nötig.").complete().delete().queueAfter(15, TimeUnit.SECONDS);
					writeFile(message, channel, amount, reportedMember, reportingUser, reason);
					
				}
				catch (NumberFormatException e) {
					System.err.println("Cought exception: log amount larger than int (LogCommand.java)");
				}
				catch (ArrayIndexOutOfBoundsException e) {
					channel.sendMessage("Falsche Formatierung: ```%report @Nutzer <reason>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
			}
			else {
				channel.sendMessage("Falsche Formatierung: ```%report @Nutzer <reason>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
		}
		else {
			channel.sendMessage("```%report @Nutzer <reason>```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
	
	//Nachrichten werden abgerufen und in eine .txt geschrieben
	private void writeFile(Message originMessage, MessageChannel channel, int amount, Member reportedMember, User reportingUser, String reason) {
		
		String path = DiscordBot.INSTANCE.getLogFilePath();
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		
		MessageHistory messageHistory = channel.getHistoryBefore(originMessage, amount).complete();
		List<Message> messageList = messageHistory.getRetrievedHistory();
		List<String> textList = new ArrayList<>();
		textList.add("Logfile - time created: " + OffsetDateTime.now().format(dateFormat) + " - guild: " + originMessage.getGuild().getName() + " - channel: #" + originMessage.getChannel().getName() + " - requested by: " + originMessage.getAuthor().getName() + " (" + originMessage.getAuthor().getId() + ")");
		textList.add("");
		textList.add("reported: " + reportedMember.getEffectiveName() + " (" + reportedMember.getId() + ") - reason: " + reason);
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
		String fileName = "log-report " + OffsetDateTime.now().format(dateFormat) + " " + channel.getName() + ".txt";
		try {
			embed.setColor(0xff00ff); //helles Lila
			embed.setTitle(":mag: User reported");
			embed.setAuthor(originMessage.getGuild().getSelfMember().getNickname());
			embed.setTimestamp(OffsetDateTime.now());
			embed.setThumbnail(originMessage.getGuild().getSelfMember().getUser().getAvatarUrl());
			embed.setDescription(reportedMember.getAsMention() + " wurde von " + originMessage.getAuthor().getAsMention() + " reported" + 
			"\n reason: *" + reason + "*\n" +
			"\n Das Log enthält die letzten " + amount + " Nachrichten aus <#" + channel.getId() + ">");
			audit.sendFile(file, fileName).embed(embed.build()).complete();
		}
		catch (IllegalArgumentException | UnsupportedOperationException e) {
			e.printStackTrace();
		}
		catch (InsufficientPermissionException e) {
			channel.sendMessage(":no_entry_sign: Dem Bot fehlt die nötigen Berechtigungen um den Befehl erfolgreich auszuführen").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
		
		// .txt wird gelöscht
		file.delete();
		
	}
}