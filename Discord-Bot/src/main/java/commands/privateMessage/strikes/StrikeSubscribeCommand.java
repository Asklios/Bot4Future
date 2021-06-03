package main.java.commands.privateMessage.strikes;

import main.java.DiscordBot;
import main.java.commands.privateMessage.PrivateCommand;
import main.java.files.interfaces.SubscribtionDatabase;
import main.java.helper.api.LocalGroup;
import main.java.helper.api.LocalGroups;
import main.java.helper.api.Strikes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class StrikeSubscribeCommand implements PrivateCommand {
    private SubscribtionDatabase db = DiscordBot.INSTANCE.subscribtionDatabase;

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
            if (db.getSubscribtionsOfUser(user.getId()).contains(group.getId())) {
                message.reply("Du hast die Ortsgruppe " + group.getName() + " bereits aboniert.").queue();
            } else {
                db.subscribe(user.getId(), group.getId());
                message.reply("Du hast nun die OG " + group.getName() + " aboniert und wirst über neue Streiks informiert.").queue();
            }
        } else {
            message.reply("Diese Ortsgruppe konnte nicht gefunden werden.").queue();
        }
    }

    private static MessageEmbed getSyntax() {
        return new EmbedBuilder()
                .setTitle("Syntax: %subscribe <OG-Name>")
                .setDescription("Mit diesem Befehl kannst du Informationen über Ortsgruppen abonieren. Somit bekommst du " +
                        "automatisch neue Streiktermine als Direktnachricht zugesandt.")
                .addField("Parameter", "- OG-Name: der Name der Ortsgruppe, " +
                        "über deren Streiks du benachrichtigt werden willst.", false)
                .build();
    }
}
