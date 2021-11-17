package main.java.listener;

import com.vdurmont.emoji.EmojiParser;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionListener extends ListenerAdapter {
    private ChannelDatabase db = new ChannelDatabaseSQLite();
    private Map<Long, Long> channelCache = new HashMap<>();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        long questionChannelId = channelCache.getOrDefault(event.getGuild().getIdLong(), 0L);

        TextChannel questionChannel = db.getQuestionChannel(event.getGuild());
        if (questionChannel != null) {
            questionChannelId = questionChannel.getIdLong();
            channelCache.put(event.getGuild().getIdLong(), questionChannelId);
        }
        if (questionChannelId == event.getChannel().getIdLong()) {
            List<String> emojis = EmojiParser.extractEmojis(event.getMessage().getContentRaw())
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

            emojis.forEach(emoji -> event.getMessage().addReaction(emoji).complete());
            event.getMessage().getEmotes().stream().filter(e -> event.getGuild().getIdLong() == e.getGuild().getIdLong())
                    .forEach(e -> event.getMessage().addReaction(e).complete());
        }
    }
}
