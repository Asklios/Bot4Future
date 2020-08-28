package main.java.commands.administation;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AdminHelpCommand implements ServerCommand{

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
			
			channel.sendMessage(member.getAsMention() + ", du findest eine Wall-of-Text in deinen PNs.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
			
			EmbedBuilder builder1 = new EmbedBuilder();
			builder1.setTitle("Botbefehle");
			builder1.setColor(0x1da64a);
			builder1.setDescription(
					
					" \r\n" +
					
					//Info Commands
					"**:clipboard:Liste der Botbefehle** (diese Nachricht) **:** --> *permission.ADMINISTRATOR* \r\n" + 
					" ```\r\n" +
					" %adminhelp``` \r\n" +
					" \r\n" +
					"**:information_source: Info: ** --> *permission.@everyone* \r\n" + 
					" ```\r\n" +
					" %info oder %help``` \r\n" +
					" \r\n" +
					"**:detective: User Info:** --> *permission.KICK_MEMBERS* \r\n" + 
					" ```\r\n" +
					" %userinfo @User1 (@User2) (@User3) (...)```\r\n" +
					" \r\n" +
					
					//Mod Commands
					"**Account bannen und PN senden:** --> *permission.BAN_MEMBERS* \r\n" + 
					" \r\n" +
					" ```%ban <reason> % @User1 (@User2) (@User3) (...) ```\r\n" +
					" \r\n" +
					"**:arrows_counterclockwise: Auf eine Nachricht reagieren/ Reaktion entfernen:** --> *permission.MESSAGE_MANAGE* \r\n" + 
					" ```\r\n" +
					" %react #channel <MessageID> :emote: (:emote2:) (...)\r\n" +
					" %unreact #channel <MessageID> :emote: :emote2: :emote3: ```\r\n" +
					" \r\n" +
					"**:bust_in_silhouette:Eine Rolle über Reaktionen vergeben:** --> *permission.MESSAGE_MANAGE* \r\n" + 
					" \r\n" +
					" ```%reactionrole #channel <MessageID> :emote: @Rolle ```\r\n" +
					" \r\n" +
					"**:roll_of_paper: Erstellen einer Rolle:** --> *permission.MANAGE_ROLES* \r\n" + 
					" ```\r\n" +
					" %createrole <Name> (<#FarbeHex>) ``` \r\n" +
					" \r\n" +
					"**:no_entry_sign: Bulk delete:** --> *permission.MESSAGE_MANAGE* \r\n" + 
					" ```\r\n" +
					" %clear <Anzahl der zu löschenden Nachrichten> ``` \r\n" +
					" \r\n" +
					" \r\n" +
					"**:scroll: Log:** -->  *permission.ADMINISTRATOR*" +
					" ```%log <Anzahl der Nachrichten (max. 100)>```\r\n" +
					" \r\n" +
					"**:mag: Report-Command:**  -->  *permission.@everyone*" +
					" ```%report @Nutzer <reason>```\r\n" +
					" \r\n" +
					
					
					" ");
			
			EmbedBuilder builder2 = new EmbedBuilder();
			builder2.setTitle("Invite-Manager");
			builder2.setColor(0x1da64a);
			builder2.setDescription(
					
					" \r\n" +
					"**:medal: Rollenvergabe bei Beitritt über bestimmte Einladung** \r\n" +  
					"--> *permission.ADMINISTRATOR* \r\n" +
					" \r\n" +
					" :rotating_light: **Achtung!:** *es ist möglich, dass ein anderer Nutzer die Rolle bekommt wenn dieser gleichzeitig dem Server beitritt.*\r\n" +
					" \r\n" +
					"Festlegen der Einladung: \r\n" +
					" ```\r\n" +
					" %specialcode <Einladungs Code> ``` \r\n" +
					" \r\n" +
					"Festlegen der Rolle: \r\n" + 
					" ```\r\n" +
					" %specialrole @Rolle ``` \r\n" +
					" \r\n" +
					" Es ist möglich, dass alle, die über die SpecialRole verfügen, per Command eine andere Rolle vergeben können. Festlegen der gebbaren Rolle: \r\n" +
					" ```%verifiablerole @Rolle``` \r\n" +
					" \r\n" +
					" Die SpecialRole Inhaber*innen verwenden dann: \r\n" +
					" ```%verify @User1 @User2``` \r\n" +
					" \r\n" +					
					"Abfrage der aktuellen Einstellung: \r\n" + 
					" ```%getspecialcode``` " +
					" ```%getspecialrole``` " +
					" ```%getverifiablerole``` " +
					" \r\n" +
					" ");
			
			EmbedBuilder builder3 = new EmbedBuilder();
			builder3.setTitle("Festlegen von Textkanälen");
			builder3.setColor(0x1da64a);
			builder3.setFooter("Bot4Future by @Asklios @Semmler");
			builder3.setTimestamp(OffsetDateTime.now());
			builder3.setDescription(
					
					" \r\n" +
					"**:gear: Festlegen des Textkanals für das Audit: ** --> *permission.ADMINISTRATOR* \r\n" + 
					" *In diesen Kanal werden Bann-Benachrichtigungen und Hinweise an die Admins geschickt.* \r\n" +
					" ```\r\n" +
					" %audit #Textkanal ``` \r\n" +
					" \r\n" +
					/*"**:gear: Festlegen des Textkanals für das Event-Audit: ** --> *permission.ADMINISTRATOR* \r\n" +
					" *In diesen Kanal wird eine Info geschickt wenn etwas auf dem Server passiert.* \r\n" +
					" ```\r\n" +
					" %eventaudit #Textkanal ``` \r\n" +
					" \r\n" + */
					"**:gear: Festlegend des Textkanals für das PN-System: ** --> *permission.ADMINISTRATOR* \r\n" + 
					" *In diesen Kanal wird eine Nachricht geschickt, wenn ein gebannter Account einen Entbannungsantrag über das PN-system stellt.* \r\n" +
					" ```\r\n" +
					" %pnchannel #Textkanal ``` \r\n" +
					" \r\n" +
					
					"**:ballot_box: Abfragen der gespeicherten Einstellungen: ** --> *gleichbleibende Berechtigungen* \r\n" + 
					" \r\n" +
					" ```%getaudit``` " +
					//"```%geteventaudit```" +
					" ```%getpnchannel``` \r\n" +
					" \r\n" +
					
					"");
		
			member.getUser().openPrivateChannel().queue((ch) -> {
			ch.sendMessage(builder1.build()).queue();
			ch.sendMessage(builder2.build()).queue();
			ch.sendMessage(builder3.build()).queue();
			});
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
}