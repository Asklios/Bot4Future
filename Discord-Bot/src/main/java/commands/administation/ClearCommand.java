package main.java.commands.administation;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClearCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String[] messageSplit = message.getContentDisplay().split("\\s+");

        // %clear 3
        if (messageSplit.length == 2) {
            try {
                int amount = Integer.parseInt(messageSplit[1]);
                if (amount > 0) {
                    try {
                        channel.purgeMessages(get(channel, amount, message));
                    } catch (ErrorResponseException e) {
                        channel.sendMessage("So viele Nachrichten kann ich garnicht finden.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                    }
                    channel.sendMessage(amount + "Nachricht(en) gelöscht.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS)); // Bestätigung wird geschickt aber nach angegebener Zeit gelöscht
                }
                else if (amount == 0) {
                    channel.sendMessage("Da weiß ich ja gar nicht wo ich jetzt anfangen soll. Wenn ich jetzt von vorne " +
                            "anfange werde ich eh nicht mehr fertig und wenn ich von hinten anfange, dann bleibt " +
                            "der Anfang liegen. Und dann nervt auch noch " + member.getAsMention() + " und möchte, dass ich an " +
                            "einem Projekt arbeite, bei welchem überhaupt nichts gemacht werden muss.").queue(m -> m.delete().queueAfter(60,TimeUnit.SECONDS));
                }
                else {
                    channel.sendMessage(">>> Zwei Mathematikprofessoren unterhalten sich vor einem Hörsaal, "
                            + "ohne dass sie in diesen hineinsehen können. Sie beobachten wie fünf Studenten "
                            + "hineingehen und dann sehen sie wie sechs wieder herauskommen. Daraufhin sagt"
                            + " einer der Mathematikprofessoren zu dem anderen:"
                            + " \"Wenn jetzt noch einer reingeht, ist der Saal leer!\"").queue(m -> m.delete().queueAfter(120,TimeUnit.SECONDS));
                }
            } catch (NumberFormatException e) {
                //e.printStackTrace(); // Konsolenausgabe Fehlermeldung
                System.err.println("Cought exception: clear amount larger than int (ClearCommand.java)");
            }
        } else {
            String praefix = "%";
            EmbedBuilder builder = new EmbedBuilder();
            channel.sendMessage("Falsche Formatierung!").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            builder.setDescription(praefix + "clear <Anzahl der zu löschenden Nachrichten>");
            channel.sendMessage(builder.build()).queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
        }

    }


    public List<Message> get(MessageChannel channel, int amount, Message originMessage) {
        List<Message> messages = new ArrayList<>();
        int i = amount + 1;

        for (Message message : channel.getIterableHistory().cache(false)) { // Nachrichten werden durchgegangen
            if (!message.isPinned() && !message.equals(originMessage)) { // angepinnte Nachrichten werden nicht gelöscht, zählt aber zu gelöschten
                messages.add(message);

            }
            if (--i <= 0) return messages; //gepinnte Nachricht wird nicht gelöscht aber mitgezählt
        }

        return messages;
    }

}
