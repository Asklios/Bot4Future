package de.java.main.listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import de.java.main.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AutoListener {
	
	public ConcurrentHashMap<String, String> autoResponse;
	
	public AutoListener() {
		
		this.autoResponse = new ConcurrentHashMap<>();
		readFile();
		
		
		
	}
		
	public void autoListen(Member member, TextChannel channel, Message message) {
		
		if (!member.getUser().equals(member.getJDA().getSelfUser())) {
			String s = this.autoResponse.get(message.getContentRaw().replaceAll(" ", "\\§").replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("\\!", "").toLowerCase()); // s = festgelegte Antwort
			
			if (s != null) {
			
				channel.sendMessage(s).queue();
			}
		}
	}
	
	private void readFile() {
		try {
			File file = new File(DiscordBot.INSTANCE.getAutoListenerFilePath());
			Scanner scanner = new Scanner(file);
			
			while (scanner.hasNextLine()) {
				
				String line = "";
				line = scanner.nextLine();
				
				String[] split = line.split("\\$");
				split[0] = split[0].replaceAll(" ", "\\§").replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("\\!", "").toLowerCase();
				if(split.length == 2) {
					
					this.autoResponse.put(split[0], split[1]);
				}
				
			}
			
			scanner.close();
		}
		
		catch(FileNotFoundException e) {
			String path = DiscordBot.INSTANCE.getAutoListenerFilePath();
			System.err.println("Could not find autoResponseFile at" + path);
			File newFile = new File(path);
			try {
				newFile.createNewFile();
				FileWriter myWriter = new FileWriter(path);
			    myWriter.write("Hier Sprüche eintragen\r\n" + "userinput$botoutput");
			    myWriter.close();
				System.out.println("New file autoResponseFile created. Filename: " + newFile.getName() + " Path: " + path);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}

