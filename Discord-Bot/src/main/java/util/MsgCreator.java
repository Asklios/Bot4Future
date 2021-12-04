package main.java.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MsgCreator {
    public static Message of(EmbedBuilder builder) {
        return of(builder.build());
    }

    public static Message of(MessageEmbed embed) {
        MessageBuilder msgBuilder = new MessageBuilder();
        msgBuilder.setEmbeds(embed);
        return msgBuilder.build();
    }
}
