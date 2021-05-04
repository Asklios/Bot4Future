package main.java.util;

import main.java.DiscordBot;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.PollDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.PollDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import org.joda.time.DateTime;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PollManager {
    private Map<String, PollSetup> setups = new HashMap<>();

    private List<String> setDescription = new ArrayList<>();
    private List<String> setName = new ArrayList<>();
    private List<String> addChoice = new ArrayList<>();
    private List<String> removeChoice = new ArrayList<>();
    private List<String> setVotesPerUser = new ArrayList<>();
    private List<String> setEndTime = new ArrayList<>();

    public final PollDatabase database = new PollDatabaseSQLite();
    private ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    public void handleReactionEvent(GenericMessageReactionEvent event) {
        if (event.isFromGuild()) {
            database.getPolls().stream().filter(poll -> event.getGuild().getId().equals(poll.getGuildId())
                    && event.getMessageId().equals(poll.getMessageId()))
                    .findFirst().ifPresent(poll -> {

            });
        }

        setups.forEach((id, setup) -> {
            if (id.equals(event.getMember().getId()) && setup.msgId.equals(event.getMessageId())) {
                String emote = event.getReactionEmote().getEmoji();
                if (emote.equals(Emojis.BACK)) {
                    String uId = event.getUserId();
                    if (setName.contains(uId)) {
                        setName.remove(uId);
                    } else if (setDescription.contains(uId)) {
                        setDescription.remove(uId);
                    } else if (addChoice.contains(uId)) {
                        addChoice.remove(uId);
                    } else if (removeChoice.contains(uId)) {
                        removeChoice.remove(uId);
                    } else if (setEndTime.contains(uId)) {
                        setEndTime.remove(uId);
                    } else if (setVotesPerUser.contains(uId)) {
                        setVotesPerUser.remove(uId);
                    }
                    setup.msg.editMessage(createSetupMessage(setup)).queue();
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(0))) {
                    setName.add(event.getMember().getId());
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Umfragenname setzen")
                                .setDescription("Gib den Anzeigenamen der Umfrage hier im Chat ein.")
                                .build()).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(1))) {
                    setDescription.add(event.getMember().getId());
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Umfragennbeschreibung setzen")
                                .setDescription("Gib die Beschreinung der Umfrage hier im Chat ein.")
                                .build()).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(2))) {
                    addChoice.add(event.getMember().getId());
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Neue Möglichkeit hinzufügen")
                                .setDescription("Gib den Text für die neue Möglichkeit ein.")
                                .build()).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(3))) {
                    if (setup.choices.size() < 1) {
                        setup.msg.getChannel().sendMessage("Es gibt noch keine Möglichkeiten, die du entfernen könntest!")
                                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    }
                    removeChoice.add(event.getMember().getId());
                    StringBuilder builder = new StringBuilder();
                    setup.choices.forEach(choice -> {
                        builder.append(setup.choices.indexOf(choice) + 1 + ": " + choice + "\n");
                    });
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Möglichkeit entfernen")
                                .setDescription("Gib die Nummer der Möglichkeit an, die gelöscht werden soll.")
                                .addField("Bestehende Möglichkeiten:", builder.toString(), false)
                                .build()).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(4))) {
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Stimmen pro Nutzer festlegen.")
                                .setDescription("Gib die Nummer der Stimmen an, die jeder Nutzer haben soll.")
                                .build()).queue();
                    });
                    setVotesPerUser.add(event.getUserId());
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(5))) {
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(new EmbedBuilder()
                                .setTitle("Endzeitpunkt.")
                                .setDescription("Gib den Zeitpunkt an, an dem die Umfrage geschlossen werden soll.\n" +
                                        "\n*Syntax:*\n" +
                                        "```\n" +
                                        "d.M.y, h:m\n" +
                                        "Beispiele: 12.10.21, 12:35\n" +
                                        "           1.2.22, 1:1\n" +
                                        "```")
                                .build()).queue();
                    });
                    setEndTime.add(event.getUserId());
                } else if (emote.equals(Emojis.READY)) {
                    DiscordBot.POOL.schedule(() -> {
                        try {
                            createPoll(setup);
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                    }, 1, TimeUnit.SECONDS);
                } else {
                    event.getReaction().removeReaction(event.getUser()).queue();
                }
            }
        });
    }

    public void handleMessageCreateEvent(MessageReceivedEvent event) {
        String id = event.getMember().getId();
        if (setups.containsKey(id)) {
            PollSetup setup = setups.get(id);
            if (!setup.channelId.equals(event.getChannel().getId())) return;
            if (setName.contains(id)) {
                setup.name = event.getMessage().getContentStripped();
                setName.remove(id);
                resetMessage(setup.msg, setup);
                event.getMessage().delete().queue();
            } else if (setDescription.contains(id)) {
                setup.description = event.getMessage().getContentStripped();
                setDescription.remove(id);
                resetMessage(setup.msg, setup);
                event.getMessage().delete().queue();
            } else if (addChoice.contains(id)) {
                if (setup.choices.contains(event.getMessage().getContentStripped())) {
                    event.getChannel().sendMessage("Diese Möglichkeit exsistiert bereits!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    addChoice.remove(id);
                } else {
                    setup.choices.add(event.getMessage().getContentStripped());
                    addChoice.remove(id);
                    resetMessage(setup.msg, setup);
                }
                event.getMessage().delete().queue();
            } else if (removeChoice.contains(id)) {
                try {
                    Integer integer = Integer.valueOf(event.getMessage().getContentRaw());
                    if (setup.choices.size() > integer || integer < 1) {
                        setup.msg.getChannel().sendMessage("Du musst eine gültige Zahl angeben!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    }
                    setup.choices.remove(integer - 1);
                    resetMessage(setup.msg, setup);
                    removeChoice.remove(id);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Du musst eine gültige Zahl angeben!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } finally {
                    event.getMessage().delete().queue();
                }
            } else if (setVotesPerUser.contains(id)) {
                try {
                    Integer cnt = Integer.valueOf(event.getMessage().getContentRaw());
                    if (cnt < 0 || cnt > setup.choices.size()) throw new NumberFormatException();
                    setup.votesPerUser = cnt;
                    setVotesPerUser.remove(id);
                    resetMessage(setup.msg, setup);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Du musst eine gültige Zahl größer als 0 und kleiner als " + setup.choices.size() + " angeben!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } finally {
                    event.getMessage().delete().queue();
                }
            } else if (setEndTime.contains(id)) {
                try {
                    DateTime time = DiscordBot.FORMATTER.parseDateTime(event.getMessage().getContentRaw());
                    if (time.isBeforeNow()) throw new IllegalArgumentException();
                    setup.endTime = time;
                    setEndTime.remove(id);
                    resetMessage(setup.msg, setup);
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage("Du musst ein gültiges Datum angeben!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } finally {
                    event.getMessage().delete().queue();
                }
            }
        }
    }

    private void resetMessage(Message msg, PollSetup setup) {
        msg.editMessage(createSetupMessage(setup)).queue();
    }

    public void initPollSetupMessage(Member member, TextChannel channel) {
        PollSetup setup = new PollSetup();
        setup.channelId = channel.getId();
        setup.guildId = member.getGuild().getId();
        setup.userId = member.getId();
        setups.put(member.getId(), setup);
        channel.sendMessage(createSetupMessage(setup)).queue(msg -> {
            setup.msgId = msg.getId();
            setup.msg = msg;
            DiscordBot.POOL.schedule(() -> {
                if (setups.values().contains(setup)) {
                    msg.editMessage(new EmbedBuilder().setTitle("Umfrage erstellen fehlgeschlagen!")
                            .setDescription("Du hast zu lange gebraucht!").build()).queue();
                    msg.clearReactions().queue();
                    setups.remove(member.getId());
                }
            }, 5, TimeUnit.MINUTES);
            List<String> emotes = Arrays.asList(Emojis.BACK, Emojis.EMOJI_LETTERS.get(0), Emojis.EMOJI_LETTERS.get(1), Emojis.EMOJI_LETTERS.get(2), Emojis.EMOJI_LETTERS.get(3), Emojis.EMOJI_LETTERS.get(4), Emojis.EMOJI_LETTERS.get(5), Emojis.READY);
            new Emojis.ReactionAdder(emotes).addReactions(msg, () -> {
            });
        });
    }

    private class PollSetup {
        public String name;
        public String channelId;
        public String msgId;
        public Message msg;
        public String description;
        public String guildId;
        public List<String> choices = new ArrayList<>();

        public int votesPerUser = 1;
        public DateTime endTime;
        public String userId;
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
                .addField("Beschreibung", pollData.description == null ? "*nicht gesetzt*" : pollData.description, false)
                .addField("Möglichkeiten", choices.toString(), false)
                .addField("Stimmen pro Nutzer", pollData.votesPerUser + (pollData.votesPerUser == 1 ? " Stimme" : " Stimmen"), true)
                .addField("Endzeitpunkt", pollData.endTime == null ? "*nicht gesetzt*" : DiscordBot.FORMATTER.print(pollData.endTime), true)
                .addField("Eigenschaften anpassen", "\uD83C\uDDE6 | Name ändern\n"
                        + "\uD83C\uDDE7 | Beschreibung ändern\n"
                        + "\uD83C\uDDE8 | Möglichkeit hinzufügen\n"
                        + "\uD83C\uDDE9 | Möglichkeit entfernen\n"
                        + "\uD83C\uDDEA | Stimmen pro Nutzer festlegen\n"
                        + "\uD83C\uDDEB | Endzeitpunkt festlegen\n"
                        + Emojis.READY + " | Umfrage speichern", false)
                .build();
    }

    private MessageEmbed createPollMessage(Poll poll) {
        StringBuilder choices = new StringBuilder();
        StringBuilder current = new StringBuilder();
        AtomicReference<Integer> totalVotes = new AtomicReference<>(0);
        poll.getChoices().forEach(choice -> {
            totalVotes.set(totalVotes.get().intValue() + choice.getVotes().size());
            choices.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " | " + choice.getText() + "\n");
        });

        poll.getChoices().forEach(choice -> {
            current.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " | ");
            if (totalVotes.get() == 0) {
                for (int i = 0; i < 12; i++) {
                    current.append(Emojis.CHART_EMPTY);
                }
                current.append("\n");
            } else {
                int percent = (choice.getVotes().size() / totalVotes.get()) * 12;
                for (int i = 0; i < percent; i++) {
                    current.append(Emojis.CHART_FULL);
                }
                for (int i = 0; i < 12 - percent; i++) {
                    current.append(Emojis.CHART_EMPTY);
                }
                current.append("\n");
            }
        });
        return new EmbedBuilder()
                .setTitle("Umfrage: " + poll.getName())
                .setDescription(poll.getDescription())
                .addField("Möglichkeiten:", choices.toString(), false)
                .addField("Jetziger Stand: (" + totalVotes.get() + " Vote" + (totalVotes.get() == 1 ? "" : "s") + ")", current.toString(), false)
                .addField("Hinweise:", "Reagiere mit den entsprechenden Buchstaben " +
                        "um für eine Möglichkeit abzustimmen. Du hast **" + poll.getVotesPerUser() + "** " +
                        (poll.getVotesPerUser() == 1 ? "Stimme\n" : "Stimmen.\n") +
                        "Wenn du deine Auswahl zurücksetzen willst, reagiere mit " + Emojis.CLOSE + "." +
                        "Um deine jetzige Auswahl zu erhalten, reagiere mit " + Emojis.INFO + "\n\n" +
                        "Der Ersteller einer Umfrage kann diese mit dem Reagieren mit " + Emojis.LOCK + " schließen.", false)
                .setFooter("Diese Umfrage ist bis " + poll.getCloseDisplay() + " geöffnet.")
                .build();
    }

    private void createPoll(PollSetup data) throws SQLException {
        setups.remove(data.userId);
        if (!isPollDataReady(data)) {
            DiscordBot.INSTANCE.jda.getTextChannelById(data.channelId).sendMessage(new EmbedBuilder()
                    .setTitle("Nicht genügend Informationen")
                    .setDescription("Eine Umfrage benötigt:\n```" +
                            "- einen Namen\n" +
                            "- eine Beschreibung\n" +
                            "- einen Endzeitpunkt\n" +
                            "- mindestens zwei Antortmöglichkeiten```")
                    .setFooter("Diese Nachricht wird in 5 Sekunden gelöscht.")
                    .build()).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        Message msg = data.msg;
        msg.clearReactions().complete();
        List<String> emojis = new ArrayList<>();
        for (int i = 0; i < data.choices.size(); i++) {
            emojis.add(Emojis.EMOJI_LETTERS.get(i));
        }
        emojis.add(Emojis.INFO);
        emojis.add(Emojis.CLOSE);
        emojis.add(Emojis.LOCK);
        Poll poll = new Poll() {
            @Override
            public String getName() {
                return data.name;
            }

            @Override
            public String getDescription() {
                return data.description;
            }

            @Override
            public List<PollChoice> getChoices() {
                return PollChoiceHelper.of(data);
            }

            @Override
            public String getGuildId() {
                return data.guildId;
            }

            @Override
            public String getMessageId() {
                return data.msgId;
            }

            @Override
            public int getVotesPerUser() {
                return data.votesPerUser;
            }

            @Override
            public long getCloseTime() {
                return data.endTime.getMillis();
            }

            @Override
            public String getCloseDisplay() {
                return DiscordBot.FORMATTER.print(data.endTime);
            }

            @Override
            public String getPollOwner() {
                return data.userId;
            }
        };
        msg.editMessage(createPollMessage(poll)).queue();
        new Emojis.ReactionAdder(emojis).addReactions(msg, () -> {
            msg.addReaction(Emojis.LOCK);
        });
        database.savePoll(poll);
    }

    private boolean isPollDataReady(PollSetup data) {
        return data.name != null && data.description != null && data.choices.size() > 1 && data.endTime != null;
    }

    private static class PollChoiceHelper {

        public static List<PollChoice> of(PollSetup setup) {
            List<PollChoice> ret = new ArrayList<>();
            setup.choices.forEach(choice -> {
                ret.add(new PollChoice() {
                    @Override
                    public int getChoiceId() {
                        return setup.choices.indexOf(choice);
                    }

                    @Override
                    public List<String> getVotes() {
                        return new ArrayList<>();
                    }

                    @Override
                    public String getText() {
                        return choice;
                    }
                });
            });
            return ret;
        }
    }
}
