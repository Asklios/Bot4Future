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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StrikeSubscribtionsCommand implements PrivateCommand {
    private SubscribtionDatabase db = DiscordBot.INSTANCE.subscribtionDatabase;

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {
        LocalGroups groups = new LocalGroups();
        if (db.getSubscribtionsOfUser(user.getId()).size() == 0) {
            message.reply("Du hast keine Ortsgruppen aboniert. Nutze dafür `%subscribe <OG>`").queue();
        } else {
            List<String> ogNames = new ArrayList<>();
            StringBuilder listString = new StringBuilder();
            db.getSubscribtionsOfUser(user.getId()).forEach(ogId -> {
                LocalGroup g = groups.getGroupById(ogId);
                if (g != null) ogNames.add(g.getName());
            });
            Collections.sort(ogNames);

            ogNames.forEach(name -> listString.append("\n- " + name));
            message.reply("Du hast folgende Ortsgruppen aboniert:\n```"
                    + listString.toString()
                    + "\n```").queue();
        }
    }
}
