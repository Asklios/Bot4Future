package main.java.commands.server.user;

import main.java.commands.server.ServerCommand;
import main.java.helper.api.LocalGroups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ListOgsCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

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
        b.setFooter(channel.getGuild().getSelfMember().getEffectiveName(), channel.getGuild().getSelfMember().getUser().getEffectiveAvatarUrl());

        for (int i = 1; i < colums.length;) {
            while (b.length() < 5000) {
                if (i >= colums.length) break;
                List<String> l = colums[i];
                b.addField("", String.join(", ", l), true);
                i++;
            }
            try {
                channel.sendMessage(b.build()).queue(m -> m.delete().queueAfter(5, TimeUnit.MINUTES));
            } catch (ErrorResponseException e) {
                //message was deleted
            }
            b.clearFields();
        }
    }
}
