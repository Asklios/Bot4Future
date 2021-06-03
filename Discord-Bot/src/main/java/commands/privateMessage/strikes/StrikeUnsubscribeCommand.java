package main.java.commands.privateMessage.strikes;

import main.java.DiscordBot;
import main.java.commands.privateMessage.PrivateCommand;
import main.java.files.interfaces.SubscribtionDatabase;
import main.java.helper.api.LocalGroup;
import main.java.helper.api.LocalGroups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class StrikeUnsubscribeCommand implements PrivateCommand {
    private final SubscribtionDatabase db = DiscordBot.INSTANCE.subscribtionDatabase;

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {
        String[] parts = message.getContentRaw().split("\\s+");

        if (parts.length != 2) {
            message.reply(getSyntax()).queue();
            return;
        }
        LocalGroups groups = new LocalGroups();
        LocalGroup group = groups.getGroupByName(parts[1]);
        if (group != null) {
            if (!db.getSubscribtionsOfUser(user.getId()).contains(group.getId())) {
                message.reply("Du hast die Ortsgruppe " + group.getName() + "gar nicht aboniert.").queue();
            } else {
                db.unsubscribe(user.getId(), group.getId());
                message.reply("Du hast nun die OG " + group.getName() + " unaboniert und wirst nicht mehr über neue Streiks informiert.").queue();
            }
        } else {
            message.reply("Diese Ortsgruppe konnte nicht gefunden werden.").queue();
        }
    }

    private static MessageEmbed getSyntax() {
        return new EmbedBuilder()
                .setTitle("Syntax: %unsubscribe <OG-Name>")
                .setDescription("Mit diesem Befehl kannst du Informationen über Ortsgruppen unabonieren. Somit bekommst du " +
                        "keine neue Streiktermine mehr als Direktnachricht zugesandt.")
                .addField("Parameter", "- OG-Name: der Name der Ortsgruppe, " +
                        "über deren Streiks du nicht mehr benachrichtigt werden willst.", false)
                .build();
    }
}
