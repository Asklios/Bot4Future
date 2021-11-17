package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class AdminHelpCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {

            EmbedBuilder builder1 = new EmbedBuilder();
            builder1.setTitle("Botbefehle");
            builder1.setColor(0x1da64a);
            builder1.setDescription(

                    " *Die Klammern <> dienen nur als Anführungszeichen und können weggelassen werden.*\r\n" +
                            " \r\n" +
                            " *Um möglichst wenig schreiben zu müssen empfiehlt es sich bei langen Commands den Inhalt in der Zwischenablage zu speichern, " +
                            "um Formatierungsfehler schnell zu beheben.* \n \n `Der Bot verwendet ausschließlich UNICODE-Emotes.` \r\n" +
                            " \r\n" +

                            //Info Commands
                            "**:clipboard:Liste der Botbefehle** (diese Nachricht) **:** --> *permission.MESSAGE_MANAGE* \r\n" +
                            " ```\r\n" +
                            " %adminhelp``` \r\n" +
                            " \r\n" +
                            "**:information_source: Info: ** --> *permission.@everyone* \r\n" +
                            " ```\r\n" +
                            " %info oder %help``` \r\n" +
                            " \r\n" +
                            "**:detective: User Info:** --> *permission.KICK_MEMBERS* \r\n" +
                            " ```\r\n" +
                            " %userinfo @User1```\r\n" +
                            " \r\n" +
                            "**:warning: Lösche alle Daten die mit dem Server verbunden sind:** --> *permission.ADMINISTRATOR* \n" +
                            "```%yesiwanttodeleteallserverdata```\n\n" +

                            //Mod Commands
                            "**:hammer: Account bannen und PN senden:** --> *permission.BAN_MEMBERS* \r\n" +
                            " ```%ban <reason> % @User1 (@User2) (@User3) (...) \r\n" +
                            "%banid <userId> <reason> \n" +
                            "%tempban @user <time> <reason>``` \n" +
                            " \r\n" +
                            "**:warning: Nutzer*in verwarnen:** --> *permission.KICK_MEMBERS*" +
                            "```%warn @user <reason>``` " +
                            "```%mute @user <time> <reason>``` \n" +
                            "*Um den mute zu beenden, kann einfach die mute Rolle von der Nutzer:in entfernt werden.* \n\n" +
                            ":mute: **Festlegen der Mute-Rolle:** --> *permission.ADMINISTRATOR* \n" +
                            "```%muterole @role \n" +
                            "%getmuterole``` \n" +
                            "*Mit diesem Command kann eine neue Mute-Rolle erstellt werden welche automatisch für die Textchannels kofiguriert wird auf welche die " +
                            "@publicRole Zugriff hat* \n" +
                            "```%newmuterole @publicRole``` \n" +

                            "**:telephone: TK-Anwesenheit:** --> *permission.MESSAGE_MANAGE*\n" +
                            "```%presence <Voice-Channel-Id> <Call-Name>\n" +
                            "%getpresence <Id oder Name> ```\n" +
                            "*Admins können alle Daten löschen die für ihren Server gespeicht wurden.* \n" +
                            "```%removeallcalldata ``` \n " +

                            "**:bar_chart: Poll-Command:** --> *permission.MESSAGE_MANAGE*\n" +
                            "```%poll```" +
                            "*Der Rest wird in der folgenden Nachricht beschrieben.*");

            EmbedBuilder builder2 = new EmbedBuilder();
            builder2.setColor(0x1da64a);
            builder2.setDescription(

                    "**:arrows_counterclockwise: Auf eine Nachricht reagieren/ Reaktion entfernen:** --> *permission.MESSAGE_MANAGE* \r\n" +
                            " ```\r\n" +
                            "%react #channel <MessageID> :emote: (:emote2:) (...)\r\n" +
                            "%unreact #channel <MessageID> :emote: :emote2: :emote3: ```\r\n" +
                            " \r\n" +
                            "**:bust_in_silhouette:Eine Rolle über Reaktionen vergeben:** --> *permission.MESSAGE_MANAGE* \r\n" +
                            " *Mit einem Emote kann nur eine Rolle vergeben werden. Ein Eintrag kann (noch) nicht überschrieben werden.*" +
                            " \r\n" +
                            " ```%reactionrole #channel <MessageID> :emote: @Rolle ```\r\n" +
                            " \r\n" +
                            "**:roll_of_paper: Erstellen einer Rolle:** --> *permission.MANAGE_ROLES* \r\n" +
                            " ```\r\n" +
                            "%createrole <Name> (<#FarbeHex>) ``` \r\n" +
                            " \r\n" +
                            "**:twisted_rightwards_arrows: Rollen sortieren:** --> *permission.ADMINISTRATOR* \r\n" +
                            " ```\r\n" +
                            "%sortroles @startrole @endrole ``` \r\n" +
                            " \r\n" +
                            "**:no_entry_sign: Bulk delete:** --> *permission.MESSAGE_MANAGE* \r\n" +
                            " ```\r\n" +
                            "%clear <Anzahl der zu löschenden Nachrichten> ``` \r\n" +
                            " \r\n" +
                            "*Nachrichten eines Nutzers löschen (überprüft die letzten 100 Nachrichten)* \r\n" +
                            "```%clearuser @user ```\r\n" +
                            " \r\n" +
                            "**:snail: SlowMode (1 min):** --> *permission.KICK_MEMBERS* \r\n" +
                            "*Bei allen Textkanälen auf welche die Rolle Zugriff hat wird der SlowMode auf 1 min gesetzt/entfernt.* \r\n" +
                            " ```\r\n" +
                            "%slow @role \r\n" +
                            "%slowend @role ``` \r\n" +
                            " \r\n" +
                            " \r\n" +
                            "**:scroll: Log:** -->  *permission.MESSAGE_MANAGE*" +
                            " ```%log <Anzahl der Nachrichten (max. 100)>```\r\n" +
                            " \r\n" +
                            "**:mag: Report-Command:**  -->  *permission.@everyone*" +
                            " ```%report @Nutzer <reason>```\r\n" +
                            " \r\n");

            EmbedBuilder builder3 = new EmbedBuilder();
            builder3.setTitle("Invite-Manager");
            builder3.setColor(0x1da64a);
            builder3.setDescription(

                    "**:medal: Rollenvergabe bei Beitritt über bestimmte Einladung** \r\n" +
                            "--> *permission.ADMINISTRATOR* \r\n" +
                            " \r\n" +
                            " :rotating_light: **Achtung!:** *es ist möglich, dass ein anderer Nutzer die Rolle bekommt wenn dieser gleichzeitig dem Server beitritt.*\r\n" +
                            " \r\n" +
                            "Festlegen der Einladung: \r\n" +
                            " ```\r\n" +
                            "%specialcode <Einladungs Code> ``` \r\n" +
                            " \r\n" +
                            "Festlegen der Rolle: \r\n" +
                            " ```\r\n" +
                            "%specialrole @Rolle ``` \r\n" +
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
                            "\n" +
                            "**:roll_of_paper: Rollenvergabe per Command**\n" +
                            "Die Commands zum Verwalten der selbst gebbaren Rollen sind jetzt bei den Slash-Commands." +
                            "Versuche /selfroles \n" +
                            "--> *permission.ADMINISTRATOR*\n\n" +
                            "```%iam <Rollenname>``` mit diesem Command kann sich jeder eine SelfRole geben." +
                            "");

            EmbedBuilder builder4 = new EmbedBuilder();
            builder4.setTitle("Festlegen von Textkanälen");
            builder4.setColor(0x1da64a);
            builder4.setFooter("Bot4Future by @Asklios @Semmler @DrDeee");
            builder4.setTimestamp(OffsetDateTime.now());
            builder4.setDescription(

                    "**:gear: Festlegen des Textkanals für das Audit: ** --> *permission.ADMINISTRATOR* \r\n" +
                            " *In diesen Kanal werden Bann-Benachrichtigungen und Hinweise an die Admins geschickt.* \r\n" +
                            " ```\r\n" +
                            "%audit #Textkanal ``` \r\n" +
                            " \r\n" +
                            "**:gear: Festlegen des Textkanals für das Event-Audit: ** --> *permission.ADMINISTRATOR* \r\n" +
                            " *In diesen Kanal wird eine Info geschickt wenn etwas auf dem Server passiert.* \r\n" +
                            " ```\r\n" +
                            " %eventaudit #Textkanal ``` \r\n" +
                            " \r\n" +
                            "**:gear: Festlegend des Textkanals für das PN-System: ** --> *permission.ADMINISTRATOR* \r\n" +
                            " *In diesen Kanal wird eine Nachricht geschickt, wenn ein gebannter Account einen Entbannungsantrag über das PN-system stellt.* \r\n" +
                            " ```\r\n" +
                            "%pmchannel #Textkanal ``` \r\n" +
                            " \r\n" +

                            "**:gear: Festlegend des Textkanals für den Umfragen-Channel: ** --> *permission.ADMINISTRATOR* \r\n" +
                            " *In diesen Channel wird auf jede Nachricht mit den verwendeten Emotes reagiert.* \r\n" +
                            " ```\r\n" +
                            "%questions #Textkanal ``` \r\n" +
                            " \r\n" +

                            "**:ballot_box: Abfragen der gespeicherten Einstellungen: ** --> *gleichbleibende Berechtigungen* \r\n" +
                            " \r\n" +
                            " ```%getaudit``` " +
                            "```%geteventaudit```" +
                            " ```%getpnchannel```" +
                            "```%questionchannel``` \r\n" +
                            " \r\n" +
                            "");

            try {
                member.getUser().openPrivateChannel().queue((ch) -> {
                    ch.sendMessage(builder1.build()).queue();
                    ch.sendMessage(builder2.build()).queue();
                    ch.sendMessage(builder3.build()).queue();
                    ch.sendMessage(builder4.build()).queue();
                });

                channel.sendMessage(member.getAsMention() + ", du findest eine Wall-of-Text in deinen PNs.")
                        .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));

            } catch (ErrorResponseException e) {
                channel.sendMessage(member.getAsMention() + ", der Bot kann dir keine PN schicken. Bitte überprüfe deine Privatsphäreeinstellungen.")
                        .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }
}