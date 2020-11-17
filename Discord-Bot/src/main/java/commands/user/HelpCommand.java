package main.java.commands.user;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class HelpCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot Info");
        //builder.setImage("https://startpage.com/av/proxy-image?piurl=https%3A%2F%2Fwww.ksb-ll.de%2Finhalte%2Fuploads%2FFotolia_139827930_M.jpg&sp=1593715011T4de5eca00c05002383bc8cbdfa556d0b5905f2aa90d0cfe0be03e3297e9c83a8");
        builder.setDescription("Commands für Admins: `%adminhelp` \r\n" // empty character
                + " \r\n"
                + " **Report:** \r\n"
                + "```%report @user <reason>``` *Verwende diesen Command um die Admins auf ein Fehlverhalten eines anderen Nutzers aufmerksam zu machen wenn sie gerade nicht online sind.* \r\n"
                + " \r\n"
                + "**Datenschutz:** \r\n"
                + "*Für die Bereitstellung der Botfunktionen speichern wir folgende Daten:* \r\n"
                + "```ServerID, festgelegte Einladung und zugehörige NutzerIDs/RollenIDs, Channel IDs in welche der Bot Nachrichten schicken soll \r\n"
                + "IDs von gebannten, entbannten, verwarnten Nutzer*innen \r\n"
                + "ServerID, ChannelID, MessageID und RoleID für jede aktive Reactionrole \r\n"
                + "ServerID, ChannelID und MessageID für Reaktionsbasierte Funktionen \r\n"
                + "ServerID, UserIDs und Uhrzeit bei Verwendung von %presence, löschen mit %removeallcalldata \r\n"
                + "Log/Report-files werden direkt nachdem sie verschickt wurden von dem Server gelöscht. ```\r\n"
                + " \r\n"
                + "```Für den Betrieb und die Bereitstellung des Servers ist die Bot UG der Messenger AG von Fridays for Future Deutschland verantwortlich ``` \r\n"
                + "**Source Code:** *https://github.com/Asklios/Bot4Future* \r\n"
                + "**Development Server:** *https://discord.gg/KumdM4e*");
        builder.setColor(0x1da64a);
        builder.setFooter("by @Asklios @Semmler");
        builder.setTimestamp(OffsetDateTime.now());

        member.getUser().openPrivateChannel().queue((ch) -> {
            try {
                ch.sendMessage(builder.build()).queue();
                channel.sendMessage(member.getAsMention() + ", bitte schau in deine PNs.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            } catch (ErrorResponseException e) {
                channel.sendMessage(member.getAsMention() + ", der Bot kann dir keine PN schicken. Bitte überprüfe deine Privatsphäreeinstellungen.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }
            catch (IllegalStateException e) {
                //
            }
        });

    }

}
