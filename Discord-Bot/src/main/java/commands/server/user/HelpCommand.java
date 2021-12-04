package main.java.commands.server.user;

import main.java.commands.server.ServerCommand;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class HelpCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Bot Info");
        builder.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl());
        builder.setDescription("Commands für Admins: `%adminhelp` \n" // empty character
            + " \n"
            + " **Report:** \n"
            + "```%report @user <reason>``` *Verwende diesen Command um die Admins auf ein Fehlverhalten eines anderen Nutzers aufmerksam zu machen wenn sie gerade nicht online sind.* \n"
            + "\n"
            + "**Command für selbstgebbare Rollen:**\n"
            + "```%iam <Rollenname>```"
            + "\n"
            + "\n"
            + "`%og <Name der Ortsgruppe>` --> *Liefert mehr Informationen zu der gesuchten Ortsgruppe.*\n"
            + "`%ogs` --> *Gibt eine Liste der Ortsgruppen aus.*\n"
            + "\n"
            + "Es gibt auch Commands für Direktnachrichten. Um mehr Infos zu bekommen schreibe dem Bot `%hilfe`\n"
            + "\n"
            + "**Datenschutz:** \n"
            + "*Für die Bereitstellung der Botfunktionen speichern wir folgende Daten:* \n"
            + "```ServerID, festgelegte Einladung und zugehörige NutzerIDs/RollenIDs, Channel IDs in welche der Bot Nachrichten schicken soll \n"
            + "IDs von gebannten, entbannten, verwarnten Nutzer*innen \n"
            + "ServerID, ChannelID, MessageID und RoleID für jede aktive Reactionrole \n"
            + "ServerID, ChannelID und MessageID für Reaktionsbasierte Funktionen \n"
            + "ServerID, UserIDs und Uhrzeit bei Verwendung von %presence, löschen mit %removeallcalldata \n"
            + "MessageID von jeder Nachricht und deren Inhalt welcher mit Daten verschlüsselt wird die nur "
            + "bekannt sind, wenn man Zugriff auf die original Nachricht hat. \n"
            + "ServerID, ChannelID von Kanälen die von dem Audit ignoriert werden.\""
            + "ServerID, Rollenname und RoleID von selbstgebbaren Rollen. "
            + "Log/Report-files werden direkt nachdem sie verschickt wurden von dem Server gelöscht. ```\n"
            + " \n"
            + "```Für den Betrieb und die Bereitstellung des Servers ist die Bot UG der Messenger AG von Fridays for Future Deutschland verantwortlich ``` \n"
            + "**Source Code:** *https://github.com/Asklios/Bot4Future* \n"
            + "**Development Server:** *https://discord.gg/KumdM4e*");
        builder.setColor(0x1da64a);
        builder.setFooter("by @Asklios @Semmler");
        builder.setTimestamp(OffsetDateTime.now());

        member.getUser().openPrivateChannel().queue((ch) -> {
            try {
                ch.sendMessage(MsgCreator.of(builder)).queue();
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
