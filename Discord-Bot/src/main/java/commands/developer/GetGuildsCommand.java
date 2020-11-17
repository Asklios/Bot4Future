package main.java.commands.developer;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetGuildsCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(member.getId())) {
            return;
        }

        JDA jda = channel.getJDA();
        List<Guild> guilds = jda.getGuilds();

        EmbedBuilder b = new EmbedBuilder();
        b.setTitle("Current Servers: ");
        b.setFooter(jda.getSelfUser().getName(), jda.getSelfUser().getEffectiveAvatarUrl());
        b.setTimestamp(OffsetDateTime.now());

        for (Guild g : guilds) {
            b.appendDescription("**ID:** " + g.getId() + " **Name:** " + g.getName() + "\n");
        }

        member.getUser().openPrivateChannel().queue(p -> {
            try {
                p.sendMessage(b.build()).queue();
            } catch (ErrorResponseException e) {
                channel.sendMessage(member.getAsMention() + ", der Bot kann dir keine PN schicken. Bitte überprüfe deine Privatsphäreeinstellungen.")
                        .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }
        });
    }
}
