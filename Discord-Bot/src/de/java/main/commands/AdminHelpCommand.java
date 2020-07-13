package de.java.main.commands;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AdminHelpCommand implements ServerCommand{

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
			
			channel.sendMessage(member.getAsMention() + ", bitte schau in deine PNs.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
		
		
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Admin Botbefehle");
			builder.setDescription(
					"**:clipboard:Liste der Botbefehle** (diese Nachricht) **:** --> *permission.ADMINISTRATOR* \r\n" + 
					" ```\r\n" +
					" %adminhelp``` \r\n" +
					" \r\n" +
					"**:arrows_counterclockwise: Auf eine Nachricht reagieren/ Reaktion entfernen:** --> *permission.MESSAGE_MANAGE* \r\n" + 
					" ```\r\n" +
					" %react #channel <MessageID> :emote: (:emote2:) (:emote3:) (...)\r\n" +
					" %unreact #channel <MessageID> :emote: :emote2: :emote3: ```\r\n" +
					" \r\n" +
					"**:roll_of_paper: Erstellen einer Rolle:** --> *permission.MANAGE_ROLES* \r\n" + 
					" ```\r\n" +
					" %createrole <Name> (<#FarbeHex>) ``` \r\n" +
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
					"Abfrage der Einstellung: \r\n" + 
					" ```\r\n" +
					" %getspecialcode \r\n" +
					" \r\n" +
					" %getspecialcoderole ``` \r\n" +
					" \r\n" +
					"**:information_source: Info: ** --> *permission.@everyone* \r\n" + 
					" ```\r\n" +
					" %info oder %help``` \r\n" +
					" \r\n" +
					"**:gear: Festlegen des Textkanals für das Audit: ** --> *permission.ADMINISTRATOR* \r\n" + 
					" *In diesen Kanal werden Bann-Benachrichtigungen und Hinweise an die Admins geschickt.* \r\n" +
					" ```\r\n" +
					" %audit #Textkanal ``` \r\n" +
					" \r\n" +
					"**:ballot_box: Abfragen des Auditkanals: ** --> *permission.ADMINISTRATOR* \r\n" + 
					" ```\r\n" +
					" %getaudit ``` \r\n" +
					" \r\n" +
					/*"**:gear: Festlegen des Textkanals für das Event-Audit: ** --> *permission.ADMINISTRATOR* \r\n" +
					" *In diesen Kanal wird eine Info geschickt wenn etwas auf dem Server passiert.* \r\n" +
					" ```\r\n" +
					" %eventaudit #Textkanal ``` \r\n" +
					" \r\n" +
					"**:ballot_box: Abfragen des Event-Auditkanals: ** --> *permission.ADMINISTRATOR* \r\n" + 
					" ```\r\n" +
					" %geteventaudit ``` \r\n" +
					" \r\n" */
					"**:detective: User Info:** --> *permission.KICK_MEMBERS* \r\n" + 
					" ```\r\n" +
					" %userinfo @User1 (@User2) (@User3) (...)```\r\n" +
					" \r\n" +
					"**:no_entry_sign: Bulk delete:** --> *permission.MESSAGE_MANAGE* \r\n" + 
					" ```\r\n" +
					" %clear <Anzahl der zu löschenden Nachrichten> ``` \r\n" +
					" ");
			builder.setColor(0x1da64a);
			builder.setFooter("by @Asklios @Semmler");
			builder.setTimestamp(OffsetDateTime.now());
		
			member.getUser().openPrivateChannel().queue((ch) -> {
			ch.sendMessage(builder.build()).queue();
			});
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
}