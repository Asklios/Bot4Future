package main.java.util;

import main.java.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PollManager {
    private List<Poll> polls = new ArrayList<>();

    private Map<String, PollSetup> setups = new HashMap<>();

    private static EmbedBuilder setupMessage = new EmbedBuilder()
            .setTitle("Neue Umfrage erstellen")
            .setDescription()

    public void handleReactionEvent(MessageReactionAddEvent event) {
        polls.stream().filter(poll -> event.isFromGuild()
                && event.getGuild().getId().equals(poll.getGuildId())
                && event.getMessageId().equals(poll.getMessageId()))
                .findFirst().ifPresent(poll -> {

            event.getReaction().removeReaction().submit();
        });
        if (setups.containsKey(event.getMessageId())) {

        }
    }

    public void handleMessageCreateEvent(MessageReceivedEvent event){
        if(setups.containsKey(event.getMessageId())){

        }
    }

    public void initPollSetupMessage(Member member, TextChannel channel) {
        PollSetup setup = new PollSetup();
        setup.guildId = member.getGuild().getId();
        channel.sendMessage(createSetupMessage(setup)).queue(msg -> {
            setups.put(member.getId(), setup);
            msg.addReaction(DiscordBot.EMOJI_LETTERS.get(0)).queue((n1) -> {
                msg.addReaction(DiscordBot.EMOJI_LETTERS.get(1)).queue((n2) -> {
                    msg.addReaction(DiscordBot.EMOJI_LETTERS.get(2)).queue((n3) -> {
                        msg.addReaction(DiscordBot.EMOJI_LETTERS.get(3)).queue((n4) -> {
                            msg.addReaction(DiscordBot.EMOJI_LETTERS.get(4)).queue((n5) ->
                                    msg.addReaction(DiscordBot.EMOJI_LETTERS.get(5)).queue()
                            );
                        });
                    });
                });
            });
        });
    }

    private class PollSetup {
        public String name;
        public String description;
        public String guildId;
        public List<String> choices = new ArrayList<>();
    }

    private static MessageEmbed createSetupMessage(PollSetup pollData) {
        StringBuilder choices = new StringBuilder(pollData.choices.size() == 0 ? "*keine Möglichkeit gesetzt*" : "");
        pollData.choices.forEach(choice -> {
            choices.append(pollData.choices.indexOf(choice) + 1 + ". " + choice + "\n");
        });
        return new EmbedBuilder()
                .setTitle("Neue Umfrage erstellen")
                .setDescription("Reagiere mit den entsprechenden Buchstaben um die Inhalte anzupassen.")
                .addField("Name", pollData.name == null ? "*nicht gesetzt*" : pollData.name, false)
                .addField("Möglichkeiten", choices.toString(), false)
                .addField("Eigenschaften anpassen", "\uD83C\uDDE6 | Name ändern\n"
                        + "\uD83C\uDDE7 | Beschreibung ändern\n"
                        + "\uD83C\uDDE8 | Möglichkeit hinzufügen\n"
                        + "\uD83C\uDDE9 | Möglichkeit entfernen\n"
                        + "\uD83C\uDDEA | Vorschau anzeigen\n"
                        + "\uD83C\uDDEB | Umfrage speichern", false)
                .build();
    }
}
