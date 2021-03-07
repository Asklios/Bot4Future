package main.java.commands.privateMessage.info;

import main.java.commands.privateMessage.PrivateCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.HashMap;

public class PmInfoCommands implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {

        HashMap<String, String> answers = new HashMap<>();

        String key = Arrays.stream(message.getContentDisplay().split("\\s+")).findFirst().get();

        String helpString = "**Folgende Commands sind als PN möglich:** \n" +
                "*Die Klammern < und > sind immer als Anführungszeichen zu verstehen*\n\n" +
                "`%hilfe` zeigt diese Info an.\n\n" +
                "`%og <Name der Ortsgruppe>` --> *Liefert mehr Informationen zu der gesuchten Ortsgruppe.*\n" +
                "`%ogs` --> *Gibt eine Liste der Ortsgruppen aus.*\n\n" +
                "weitere Commands:\n" +
                "`%webseite`, `%app`, `%instagram`, `%facebook`, `%twitter`, `%youtube`, `%discord`, `%ortsgruppen`, `%spenden`";

        answers.put("%test", "Hallo " + user.getName() + "! Schön, dass du mir schreibst.");
        answers.put("%webseite", "Hier findest du unsere Webseite: https://fridaysforfuture.de/");
        answers.put("%app", "Hier kannst du die App herunterladen: https://app-for-future.de/download/");
        answers.put("%instagram", "Das ist der Link zu unserem Instagram Profil: https://www.instagram.com/fridaysforfuture.de");
        answers.put("%insta", "Das ist der Link zu unserem Instagram Profil: https://www.instagram.com/fridaysforfuture.de");
        answers.put("%facebook", "Hier findest du unsere Facebook-Seite: https://www.facebook.com/fridaysforfuture.de/");
        answers.put("%twitter", "Hier geht's zu unserem Twitter Account: https://twitter.com/FridayForFuture");
        answers.put("%youtube", "Hier findest du unseren YouTube Kanal: https://www.youtube.com/c/FridaysForFutureDE");
        answers.put("%discord", "Hier ist eine Einladung für den FFF-Discord-Server: https://discord.gg/RPN9Dxm");
        answers.put("%ortsgruppen", "Hier kannst du deine Ortsgruppe (OG) finden: https://fridaysforfuture.de/regionalgruppen/");
        answers.put("%spenden", "Unterstütze uns mit einer Spende: https://fridaysforfuture.de/spenden/");
        answers.put("%hilfe", helpString);
        answers.put("%commands", helpString);
        answers.put("%info", helpString);

        channel.sendMessage(answers.get(key)).queue();
    }
}
