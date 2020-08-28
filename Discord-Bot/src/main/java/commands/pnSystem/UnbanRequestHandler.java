package main.java.commands.pnSystem;

import java.time.OffsetDateTime;
import java.util.HashMap;

import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class UnbanRequestHandler {
	
	private static HashMap<Long, UnbanRequest> users = new HashMap<>();
	
	public static void handle(MessageReceivedEvent event) {
		
		String message = event.getMessage().getContentDisplay();
		
		PrivateChannel channel = event.getPrivateChannel();
		User user = event.getPrivateChannel().getUser();
		long userID = user.getIdLong();
		//System.out.println(userID);
	
		if (!users.containsKey(userID)) {
	
			if (message.startsWith("%unban")) {
				users.put(userID, new UnbanRequest(userID));
				channel.sendMessage("Für welchen Server möchtest du einen Entbannungsantrag stellen? ```ServerID``` \n \n "
						+ ":rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
				return;
			}
			else {
				channel.sendMessage("Wenn du einen Entbannungsantrag stellen möchtest beginne mit ```%unban``` \n "
						+ "Um den Vorgang abzubrechen nutze ```exit``` \n \n :rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
			}
		}
		else {
			UnbanRequest request = users.get(userID);
			if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("cancel")) {
				users.remove(userID);
				channel.sendMessage("**Vorgang abgebrochen**").complete();
				return;
			}
			if (request.getGuildID() == 0) {
				// überprüfen ob Serverexistiert/ gebannt ist
				if (message.length() == 18) {
					try {
						Guild guild = event.getJDA().getGuildById(message);
						if (!guild.equals(null)) {
							//check if user already committed a request on the serverId
							if(GuildDataXmlReadWrite.readUnbanRequestValue(userID, Long.parseLong(message)) == false) {
								try {
									guild.retrieveBanById(userID).queue(null, new ErrorHandler()
											.handle(
													ErrorResponse.UNKNOWN_BAN, (e) -> {
														channel.sendMessage("**Du bist auf diesem Server nicht gebannt.**").queue(); 
														users.remove(userID); 
														return;
													}));
		
									long serverID = guild.getIdLong();
									request.setGuildID(serverID);
									
									channel.sendMessage("Bitte begründe warum du möchtest, dass dein Bann aufgehoben wird.").queue();
									
								}
								catch (InsufficientPermissionException e) {
									TextChannel audit = guild.getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(guild.getIdLong()));
									audit.sendMessage(":no_entry_sign: Wegen fehlenden Berechtigungen konnte ein Entbannungsantrag nicht bearbeitet werden. "
											+ "Der Bot benötigt die Berechtigung: ban_members").queue();
								}
							} else {
								channel.sendMessage("Du hast auf diesen Server bereits einen Antrag gestellt.").queue();
								users.remove(userID); 
								return;
							}
						}
						else {
							channel.sendMessage("Der Server konnte nicht gefunden werden, bitte überprüfe die ID.").queue();
						}
					}
					catch (NumberFormatException e) {
						channel.sendMessage("**Falsche Formatierung:** \n Die Server-Id ist eine 18-stellige Nummer, du findest sie in der Bannbenachrichtigung").queue();
					}
					return;
				}
			}
			else if (request.getReason() == null) {
				request.setReason(message);
				users.remove(userID);
	
				
				
				GuildDataXmlReadWrite.writeUnbanRequestValue(userID, request.getGuildID(), true);
	
				Guild guild = event.getJDA().getGuildById(request.getGuildID());
				String reason = request.getReason();
				TextChannel audit = null;
				
				if (!guild.getTextChannelById(GuildDataXmlReadWrite.readPnChannelId(guild.getIdLong())).equals(null)) {
					audit = guild.getTextChannelById(GuildDataXmlReadWrite.readPnChannelId(guild.getIdLong()));
					channel.sendMessage("Dein Entbannungsantrag wurde an die Verantwortlichen gesendet").queue();
				}
				else if (!guild.getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(guild.getIdLong())).equals(null)) {
					audit = guild.getTextChannelById(GuildDataXmlReadWrite.readAuditChannelId(guild.getIdLong()));
					channel.sendMessage("Dein Entbannungsantrag wurde an die Verantwortlichen gesendet").queue();
				}
				else {
					channel.sendMessage("Es konnte keine Nachricht gesendet werden, da der Server die Funktion nicht aktiviert hat.").queue();
				}
	
				EmbedBuilder b = new EmbedBuilder();
				b.setColor(0xff00ff); //helles Lila
				b.setTitle(":innocent: Entbannungsantrag");
				b.setTimestamp(OffsetDateTime.now());
				b.setThumbnail(channel.getUser().getAvatarUrl() == null ? channel.getUser().getDefaultAvatarUrl() : channel.getUser().getAvatarUrl());
				b.setDescription("**Nutzer:** " + channel.getUser().getName() + "(" + userID + ") \n \n"
						+ "**Begründung:** " + reason + "\n \n **Gebannt wegen:** " + guild.retrieveBanById(userID).complete().getReason());
				try {
					audit.sendMessage(b.build()).queue();
				}
				catch (IllegalArgumentException e) {
					
				}
			}
		}
		return;
	}
}
