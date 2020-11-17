package main.java.commands.administation;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RemoveSlowModeCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.KICK_MEMBERS)) {
            channel.sendMessage("Du musst Kick_Members besitzen um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //%slowend @role @role2 ...
        Guild guild = message.getGuild();
        List<Role> mentionedRoles = message.getMentionedRoles();
        List<TextChannel> textChannels = guild.getTextChannels();
        ArrayList<TextChannel> publicChannel = new ArrayList<>();

        if (mentionedRoles.isEmpty()) {
            channel.sendMessage("Du musst mindestens eine Rolle angeben deren Textchannels von dem Slowmode befreit werden sollen.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        for (Role r : mentionedRoles) {
            for (TextChannel c : textChannels) {
                if (r.hasAccess(c)) {
                    publicChannel.add(c);
                }
            }
        }

        for (TextChannel c: publicChannel) {
            c.getManager().setSlowmode(0).queue();
        }

        channel.sendMessage("In folgenden Channeln wurde der Slowmode entfernt: " + publicChannel.stream()
                .map(s -> new StringBuffer(s.getAsMention())).collect(Collectors.joining(" "))).queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));

    }
}
