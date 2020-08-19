package main.java.commands.administation;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class HelpCommand implements ServerCommand{

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		
		channel.sendMessage(member.getAsMention() + ", bitte schau in deine PNs.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Bot Info");
		//builder.setImage("https://startpage.com/av/proxy-image?piurl=https%3A%2F%2Fwww.ksb-ll.de%2Finhalte%2Fuploads%2FFotolia_139827930_M.jpg&sp=1593715011T4de5eca00c05002383bc8cbdfa556d0b5905f2aa90d0cfe0be03e3297e9c83a8");
		builder.setDescription("Commands für Admins: %adminhelp \r\n" // empty character
				+ " \r\n"
				+ "Verwende ```%report @user <reason>``` um die Admins auf ein Fehlverhalten eines anderen Nutzers aufmerksam zu machen wenn sie gerade nicht online sind."
				+ " \r\n"
				+ "**Datenschutz:** \r\n"
				+ "```Wir speichern folgende Daten auf einem Server in Deutschland: \r\n"
				+ "ServerID, festgelegte Einladung und zugehörige NutzerIDs/RollenIDs, Channel IDs in welche der Bot Nachrichten schicken soll, IDs von gebannten und entbannten Nutzer*innen \r\n"
				+ "Log/Report-files werden direkt nachdem sie verschickt wurden von dem Server gelöscht. ```\r\n"
				+ " \r\n"
				+ "```Für den Betrieb und die Bereitstellung des Servers ist die Bot UG der Messenger AG zuständig ``` ");
		builder.setColor(0x1da64a);
		builder.setFooter("by @Asklios @Semmler");
		builder.setTimestamp(OffsetDateTime.now());
		
		member.getUser().openPrivateChannel().queue((ch) -> {
			ch.sendMessage(builder.build()).queue();
			
		});
		
	}

}
