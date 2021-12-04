package main.java.commands.privateMessage.info;

import main.java.commands.privateMessage.PrivateCommand;
import main.java.util.MsgCreator;
import main.java.helper.api.LocalGroups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListOgsPmCommand implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {
        List<String> groupNames = new ArrayList<>();
        Arrays.stream(new LocalGroups().getLocalGroups()).forEach(g -> groupNames.add(g.getName()));
        groupNames.sort(Comparator.naturalOrder());

        int fieldLength = 60;
        List<String>[] colums = new List[groupNames.size()/fieldLength + 2];

        int j = 0;
        int k = 0;
        for (int i = 0; i < colums.length; i++) {
            colums[i] = new ArrayList();
            while (j*fieldLength > k) {
                if (k >= groupNames.size()) break;
                colums[i].add(groupNames.get(k));
                k++;
            }
            j++;
        }

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0x1DA64A);
        b.setAuthor("Ortsgruppen FFF Deutschland (" + groupNames.size() + ")", "https://fridaysforfuture.de/regionalgruppen/",
                "https://www.google.com/s2/favicons?sz=128&domain_url=https://fridaysforfuture.de/");
        b.setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getEffectiveAvatarUrl());

        for (int i = 1; i < colums.length;) {
            while (b.length() < 5000) {
                if (i >= colums.length) break;
                List<String> l = colums[i];
                b.addField("", l.stream().collect(Collectors.joining(", ")), true);
                i++;
            }
            channel.sendMessage(MsgCreator.of(b)).queue();
            b.clearFields();
        }
    }
}
