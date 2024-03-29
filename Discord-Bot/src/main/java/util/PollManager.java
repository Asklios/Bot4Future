package main.java.util;

import main.java.DiscordBot;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.PollDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.PollDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PollManager {
    private static final Random RANDOM = new Random();
    private final Map<String, PollSetup> setups = new HashMap<>();

    private final List<String> setDescription = new ArrayList<>();
    private final List<String> setName = new ArrayList<>();
    private final List<String> addChoice = new ArrayList<>();
    private final List<String> removeChoice = new ArrayList<>();
    private final List<String> setVotesPerUser = new ArrayList<>();
    private final List<String> setEndTime = new ArrayList<>();
    private final List<String> setTargetChannel = new ArrayList<>();

    public final PollDatabase database = new PollDatabaseSQLite();
    private final ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    public PollManager() {
        DiscordBot.POOL.scheduleAtFixedRate(() -> {
            List<String> closedPolls = new ArrayList<>();
            database.getPolls().forEach(poll -> {
                if (poll.getCloseTime() < System.currentTimeMillis()) {
                    Guild g = DiscordBot.INSTANCE.jda.getGuildById(poll.getGuildId());
                    g.getTextChannels().forEach(channel -> {
                        channel.retrieveMessageById(poll.getMessageId()).submit().thenAccept(msg -> {
                            database.setPollVoters(poll, generateVoterList(poll));
                            logPoll(poll, g, true, true, true);
                            msg.editMessage(MsgCreator.of(createPollResultMessage(poll))).queue();
                            msg.clearReactions().queue();
                        }).exceptionally((t) -> null);
                    });
                    closedPolls.add(poll.getMessageId());
                }
            });
            closedPolls.forEach(toClose -> database.deletePoll(toClose));
            try {
                database.saveVotes();
            } catch (SQLException exception) {
                System.out.println("ERROR: Could not save votes");
                exception.printStackTrace();
            }
        }, 5, 30, TimeUnit.SECONDS);
    }

    public void saveVotes() {
        try {
            database.saveVotes();
        } catch (SQLException exception) {
            System.out.println("ERROR: Could not save votes");
            exception.printStackTrace();
        }
    }

    public void handleReactionEvent(GenericMessageReactionEvent event) {
        // Don't handle custom emotes
        if (event.getReaction().getReactionEmote().isEmote()) return;
        if (event.isFromGuild()) {
            database.getPolls().stream().filter(poll -> event.getGuild().getId().equals(poll.getGuildId())
                            && event.getMessageId().equals(poll.getMessageId()))
                    .findFirst().ifPresent(poll -> {
                        if (poll.getCloseTime() < System.currentTimeMillis() || event instanceof MessageReactionRemoveEvent || event.getUser().isBot()) {
                            return;
                        }
                        ZonedDateTime zdt = ZonedDateTime.of(event.getMember().getTimeJoined().toLocalDateTime(), ZoneId.systemDefault());
                        boolean isTwoWeeks = (System.currentTimeMillis() - zdt.toInstant().toEpochMilli() + 2 * 60 * 60 * 1000) > (14 * 24 * 60 * 60 * 1000);
                        if (!isTwoWeeks) {
                            event.getMember().getUser().openPrivateChannel().queue(pChannel -> {
                                pChannel.sendMessage("Du musst seit mindestens zwei Wochen auf " + event.getGuild().getName() + " sein, um an Umfragen teilzunehmen.").queue();
                            });
                            event.getReaction().removeReaction(event.getUser()).queue();
                            return;
                        }
                        String emote = event.getReactionEmote().getEmoji();
                        event.getReaction().removeReaction(event.getUser()).queue();
                        if (Emojis.EMOJI_LETTERS.contains(emote)) {
                            int choice = Emojis.EMOJI_LETTERS.indexOf(emote);
                            if (poll.getChoices().size() < choice) {
                                event.getChannel().sendMessage("Ungültige Option, bitte wähle eine richtige.")
                                        .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                            } else {
                                int cnt = (int) poll.getChoices().stream().filter(c -> c.getVotes().contains(event.getUserId())).count();
                                if (cnt >= poll.getVotesPerUser()) {
                                    event.getUser().openPrivateChannel().queue(channel -> {
                                        channel.sendMessage("Du hast bereits " + cnt + " Stimmen abgegeben!").queue();
                                    });
                                } else {
                                    if (poll.getChoices().get(choice).getVotes().contains(event.getUserId())) {
                                        event.getUser().openPrivateChannel().queue(channel -> {
                                            channel.sendMessage("Du hast bereits für diese Option angestimmt!").queue();
                                        });
                                    } else {
                                        poll.getChoices().get(choice).getVotes().add(event.getUserId());
                                        event.retrieveMessage().queue(msg -> msg.editMessage(MsgCreator.of(createPollMessage(poll))).queue());
                                    }
                                }
                            }
                        } else if (Emojis.LOCK.equals(emote)) {
                            if (poll.getPollOwner().equalsIgnoreCase(event.getUserId()) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                event.retrieveMessage().queue(msg -> {
                                    msg.editMessage(MsgCreator.of(createPollResultMessage(poll))).queue();
                                    msg.clearReactions().queue();
                                });
                                database.setPollVoters(poll, generateVoterList(poll));
                                logPoll(poll, event.getGuild(), true, true, true);
                                database.deletePoll(poll.getMessageId());
                            }
                        } else if (Emojis.CLOSE.equalsIgnoreCase(emote)) {
                            poll.getChoices().forEach(pollChoice -> {
                                pollChoice.getVotes().remove(event.getUserId());
                            });
                            event.retrieveMessage().queue(msg -> msg.editMessage(MsgCreator.of(createPollMessage(poll))).queue());
                            event.getUser().hasPrivateChannel();
                            event.getUser().openPrivateChannel().queue(channel -> {
                                channel.sendMessage("Deine Auswahl wurde zurückgesetzt.").queue();
                            });
                            event.getReaction().removeReaction(event.getUser()).queue();
                        } else if (Emojis.INFO.equalsIgnoreCase(emote)) {
                            List<String> selected = new ArrayList<>();
                            poll.getChoices().forEach(choice -> {
                                if (choice.getVotes().contains(event.getUserId())) {
                                    selected.add(choice.getText());
                                }
                            });

                            StringBuilder builder = new StringBuilder("Du hast folgende Optionen ausgewählt:\n```");
                            if (selected.size() == 0) {
                                builder.append("\n (Du hast nichts ausgewählt!)");
                            } else {
                                selected.forEach(s -> builder.append("\n- " + s));
                            }
                            builder.append("\n```");
                            event.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(builder.toString()).queue());
                        } else if (Emojis.VIEW.equalsIgnoreCase(emote)) {
                            if (event.getMember().getId().equalsIgnoreCase(poll.getPollOwner())
                                    || event.getMember().hasPermission(Permission.ADMINISTRATOR)
                                    || event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                                event.getMember().getUser().openPrivateChannel().queue(pChannel -> {
                                    sendVoters(poll, pChannel, null);
                                });
                            } else {
                                event.getMember().getUser().openPrivateChannel().queue(pChannel -> {
                                    pChannel.sendMessage("Nur Admins, Mods und Umfragenersteller können sich die Teilnehmenden anzeigen lassen.");
                                });
                            }
                        }

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
                    } else if (setTargetChannel.contains(uId)) {
                        setTargetChannel.remove(uId);
                    } else setVotesPerUser.remove(uId);
                    setup.msg.editMessage(MsgCreator.of(createSetupMessage(setup))).queue();
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(0))) {
                    setName.add(event.getMember().getId());
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Umfragenname setzen")
                                .setDescription("Gib den Anzeigenamen der Umfrage hier im Chat ein.")
                        )).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(1))) {
                    setDescription.add(event.getMember().getId());
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Umfragennbeschreibung setzen")
                                .setDescription("Gib die Beschreinung der Umfrage hier im Chat ein.")
                        )).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(2))) {
                    if (setup.choices.size() == 17) {
                        event.getChannel().sendMessage("Du kannst maximal 17 Möglichkeiten nutzen!")
                                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    } else {
                        addChoice.add(event.getMember().getId());
                        event.retrieveMessage().queue(msg -> {
                            msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                    .setTitle("Neue Möglichkeit hinzufügen")
                                    .setDescription("Gib den Text für die neue Möglichkeit ein.")
                            )).queue();
                        });
                    }
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
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Möglichkeit entfernen")
                                .setDescription("Gib die Nummer der Möglichkeit an, die gelöscht werden soll.")
                                .addField("Bestehende Möglichkeiten:", builder.toString(), false)
                        )).queue();
                    });
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(4))) {
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Stimmen pro Nutzer festlegen.")
                                .setDescription("Gib die Nummer der Stimmen an, die jeder Nutzer haben soll.")
                        )).queue();
                    });
                    setVotesPerUser.add(event.getUserId());
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(5))) {
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Endzeitpunkt")
                                .setDescription("Gib den Zeitpunkt an, an dem die Umfrage geschlossen werden soll.\n" +
                                        "\n*Syntax:*\n" +
                                        "```\n" +
                                        "d.M.y, h:m\n" +
                                        "Beispiele: 12.10.21, 12:35\n" +
                                        "           1.2.22, 1:1\n" +
                                        "```")
                        )).queue();
                    });
                    setEndTime.add(event.getUserId());
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(6))) {
                    event.retrieveMessage().queue(msg -> {
                        msg.editMessage(MsgCreator.of(new EmbedBuilder()
                                .setTitle("Ziel setzen")
                                .setDescription("Gebe den Channelnamen ein, in dem die Umfrage gesendet werden soll.\n" +
                                        "Beachte:\n" +
                                        "```\n" +
                                        "  - beginnne den Channelnamen mit #\n" +
                                        "  - du musst in dem Channel schreiben können\n" +
                                        "```")
                        )).queue();
                    });
                    setTargetChannel.add(event.getUserId());
                } else if (emote.equals(Emojis.EMOJI_LETTERS.get(7))) {
                    setup.showVotes = !setup.showVotes;
                    resetMessage(setup.msg, setup);
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

        if (event instanceof MessageReactionRemoveEvent ||
                !event.getReactionEmote().getEmoji().equals(Emojis.VIEW)) return;
        PollDatabase.PollVoterList list = database.getPollVoters(event.getGuild().getId(), event.getMessageId());
        if (list != null) {
            event.getReaction().removeReaction(event.getUser()).queue();
            if (event.getUser().getId().equals(list.getPollOwner())
                    || event.getMember().hasPermission(Permission.ADMINISTRATOR)
                    || event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                event.getUser().openPrivateChannel().queue(channel -> {
                    sendVoters(null, channel, list.getContent());
                });
            } else {
                event.getUser().openPrivateChannel().queue(channel -> {
                    channel.sendMessage("Nur Admins, Mods und Umfragenersteller können sich die" +
                            " Teilnehmenden anzeigen lassen.");
                });
            }
        }
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
                String description = event.getMessage().getContentRaw();
                if (description.length() > 2048) {
                    event.getChannel().sendMessage("Die Beschreibung kann maximal 2048 Zeichen lang sein!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } else {
                    setup.description = event.getMessage().getContentStripped();
                    setDescription.remove(id);
                    resetMessage(setup.msg, setup);
                }
                event.getMessage().delete().queue();
            } else if (addChoice.contains(id)) {
                String choice = event.getMessage().getContentRaw();
                if (setup.choices.contains(choice)) {
                    event.getChannel().sendMessage("Diese Möglichkeit exsistiert bereits!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } else if (setup.choices.stream().collect(Collectors.joining()).length() > 950) {
                    event.getChannel().sendMessage("Da Discord nur Nachrichten bis zu einer bestimmten Länge " +
                                    "zulässt, musst du darauf achten, dass alle Möglichkeiten zusammen max. 950 Zeichen lang sind.\n" +
                                    "Nutze sonst die Beschreibung mit, bzw. erkläre die Abstimmung in einer Extra-Nachricht\n\n" +
                                    "*Hinweis: das Limit liegt bei 950 Zeichen, obwohl das von Discord festgelegte bei 1024 liegt, da " +
                                    "dieses auch noch Formatierungen von mir bekommt.*")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } else {
                    setup.choices.add(event.getMessage().getContentStripped());
                    addChoice.remove(id);
                    resetMessage(setup.msg, setup);
                }
                event.getMessage().delete().queue();
            } else if (removeChoice.contains(id)) {
                try {
                    Integer integer = Integer.valueOf(event.getMessage().getContentRaw());
                    if (setup.choices.size() > integer || 1 > integer) {
                        setup.msg.getChannel().sendMessage("Du musst eine gültige Zahl angeben!").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    } else {
                        setup.choices.remove(integer - 1);
                        if (setup.choices.size() > setup.votesPerUser) setup.votesPerUser = setup.votesPerUser - 1;
                        resetMessage(setup.msg, setup);
                        removeChoice.remove(id);
                    }
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
                event.getMessage().delete().queue();
                try {
                    DateTime time = DiscordBot.FORMATTER.parseDateTime(event.getMessage().getContentRaw());
                    if (time.isBeforeNow()) {
                        event.getChannel().sendMessage("Du musst ein gültiges Datum angeben!")
                                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                    } else {
                        setup.endTime = time;
                        setEndTime.remove(id);
                        resetMessage(setup.msg, setup);
                    }
                } catch (IllegalArgumentException e) {
                    event.getChannel().sendMessage("Du musst ein gültiges Datum angeben!")
                            .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                }
            } else if (setTargetChannel.contains(id)) {
                if (event.getMessage().getMentionedChannels().size() != 1) {
                    event.getChannel().sendMessage("DU musst exakt EINEN Textchannel angeben.").queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                } else {
                    TextChannel channel = event.getMessage().getMentionedChannels().get(0);
                    if (event.getMember().getPermissions(DiscordBot.INSTANCE.jda.getGuildChannelById(channel.getId())).contains(Permission.MESSAGE_SEND)) {
                        setup.targetChannel = channel;
                        setTargetChannel.remove(event.getMember().getId());
                        resetMessage(setup.msg, setup);
                    } else {
                        event.getChannel().sendMessage("Du hast nicht das Recht in dem angegebenen Channel Nachrichten zu schreiben!").queue(
                                msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS)
                        );
                    }

                }
                event.getMessage().delete().queue();
            }
        }
    }

    private void resetMessage(Message msg, PollSetup setup) {
        msg.editMessage(MsgCreator.of(createSetupMessage(setup))).queue();
    }

    public void initPollSetupMessage(Member member, GuildMessageChannel channel) {
        PollSetup setup = new PollSetup();
        setup.channelId = channel.getId();
        setup.guildId = member.getGuild().getId();
        setup.userId = member.getId();
        setups.put(member.getId(), setup);
        channel.sendMessage(MsgCreator.of(createSetupMessage(setup))).queue(msg -> {
            setup.msgId = msg.getId();
            setup.msg = msg;
            DiscordBot.POOL.schedule(() -> {
                if (setups.values().contains(setup)) {
                    msg.editMessage(MsgCreator.of(new EmbedBuilder().setTitle("Umfrage erstellen fehlgeschlagen!")
                            .setDescription("Du hast zu lange gebraucht!"))).queue();
                    msg.clearReactions().queue();
                    setups.remove(member.getId());
                }
            }, 15, TimeUnit.MINUTES);
            List<String> emotes = Arrays.asList(
                    Emojis.BACK,
                    Emojis.EMOJI_LETTERS.get(0),
                    Emojis.EMOJI_LETTERS.get(1),
                    Emojis.EMOJI_LETTERS.get(2),
                    Emojis.EMOJI_LETTERS.get(3),
                    Emojis.EMOJI_LETTERS.get(4),
                    Emojis.EMOJI_LETTERS.get(5),
                    Emojis.EMOJI_LETTERS.get(6),
                    Emojis.EMOJI_LETTERS.get(7),
                    Emojis.READY
            );
            new Emojis.ReactionAdder(emotes).addReactions(msg, () -> {
            });
        });
    }

    private void logPoll(Poll poll, Guild guild, boolean logState, boolean logVoters, boolean isClosed) {
        StringBuilder choices = new StringBuilder();
        StringBuilder current = new StringBuilder();
        float totalVotes = poll.getChoices().stream().map(PollChoice::getVotes).collect(Collectors.summingInt(List::size));
        poll.getChoices().forEach(choice -> {
            choices.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " | " + choice.getText() + "\n");
        });

        poll.getChoices().forEach(choice -> {
            current.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " │");
            if (choice.getVotes().size() == 0) {
                for (int i = 0; i < 12; i++) {
                    current.append(Emojis.CHART_EMPTY);
                }
                current.append("│ (" + choice.getVotes().size() + ")\n");
            } else {
                StringBuilder local = new StringBuilder();
                float percent = (choice.getVotes().size() / totalVotes) * 12;
                for (int i = 0; i < percent; i++) local.append(Emojis.CHART_FULL);
                while (local.length() != 12) {
                    local.append(Emojis.CHART_EMPTY);
                }
                current.append(local).append("│ (" + choice.getVotes().size() + ")\n");
            }
        });

        TextChannel channel = channelDatabase.getAuditChannel(guild);
        if (channel != null) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle((isClosed ? "Umfrage geschlossen:" : "Neue Umfrage erstellt: " + poll.getName()))
                    .setDescription(poll.getDescription())
                    .addField("Ersteller", poll.getPollOwner(), false)
                    .addField("Stand anzeigen", poll.areVotesVisible() ? "Ja" : "Nein", false)
                    .addField("Schließt", poll.getCloseDisplay(), false)
                    .addField("Votes per User", poll.getVotesPerUser() + "", false)
                    .addField("Möglichkeiten", choices.toString(), false)
                    .setFooter(poll.getPollOwner(),
                            DiscordBot.INSTANCE.jda.getUserById(poll.getPollOwner()).getAvatarUrl());
            if (logState) {
                builder.addField("Ergebnis: (" + (int) totalVotes + " Stimme" + (totalVotes == 1 ? "" : "n") + ")", current.toString(), false);
            }
            if (logVoters) {
                this.sendVoters(poll, channel, "");
            }
            channel.sendMessage(MsgCreator.of(builder.build())).queue();
        }
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
        public TextChannel targetChannel;
        public boolean showVotes = true;
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
                .addField("Zielchannel", pollData.targetChannel == null ? "*nicht gesetzt*" : pollData.targetChannel.getAsMention(), true)
                .addField("Stand anzeigen", pollData.showVotes ? "Ja" : "Nein", false)
                .addField("Eigenschaften anpassen", "\uD83C\uDDE6 | Name ändern\n"
                        + "\uD83C\uDDE7 | Beschreibung ändern\n"
                        + "\uD83C\uDDE8 | Möglichkeit hinzufügen\n"
                        + "\uD83C\uDDE9 | Möglichkeit entfernen\n"
                        + "\uD83C\uDDEA | Stimmen pro Nutzer festlegen\n"
                        + "\uD83C\uDDEB | Endzeitpunkt festlegen\n"
                        + "\uD83C\uDDEC | Zielchannel festlegen\n"
                        + "\uD83C\uDDED | Stand " + (pollData.showVotes ? "verstecken" : "anzeigen") + "\n"
                        + Emojis.READY + " | Umfrage speichern", false)
                .build();
    }

    private void sendVoters(Poll poll, MessageChannel channel, String content) {
        CompletableFuture.runAsync(() -> {
            File tempFolder = new File("temp");
            tempFolder.mkdirs();
            File voterFile = new File(tempFolder, "voters" + RANDOM.nextInt(100000) + ".txt");
            if (voterFile.exists()) {
                sendVoters(poll, channel, content);
            } else {
                try {
                    voterFile.createNewFile();
                    FileWriter writer = new FileWriter(voterFile);
                    if (poll == null)
                        writer.write(content);
                    else
                        writer.write(generateVoterList(poll));
                    writer.close();

                    channel.sendFile(voterFile, "voters.txt").submit().thenAccept(msg -> {
                        voterFile.delete();
                    });
                } catch (IOException e) {
                    System.out.println("Could not create voterfile:");
                    e.printStackTrace();
                }

            }
        });
    }

    private String generateVoterList(Poll poll) {
        Map<String, Integer> voters = new HashMap<>();
        List<String> voterDisplays = new ArrayList<>();
        poll.getChoices().forEach(choice -> {
            choice.getVotes().forEach(voter -> {
                if (voters.containsKey(voter)) {
                    voters.put(voter, voters.get(voter) + 1);
                } else {
                    voters.put(voter, 1);
                }
            });
        });
        voters.forEach((voter, votes) -> {
            Member m = DiscordBot.INSTANCE.jda.getGuildById(poll.getGuildId()).getMemberById(voter);
            String display = m.getUser().getName() + "#"
                    + m.getUser().getDiscriminator()
                    + " (" + m.getId() + ") | "
                    + m.getEffectiveName() + ": " + votes + " Votes\n";
            voterDisplays.add(display);
        });
        Collections.sort(voterDisplays);
        return voterDisplays.stream().collect(Collectors.joining(" "));
    }

    private MessageEmbed createPollMessage(Poll poll) {
        StringBuilder choices = new StringBuilder();
        StringBuilder current = new StringBuilder();
        float totalVotes = poll.getChoices().stream().map(PollChoice::getVotes).collect(Collectors.summingInt(List::size));
        poll.getChoices().forEach(choice -> {
            choices.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " | " + choice.getText() + "\n");
        });

        poll.getChoices().forEach(choice -> {
            current.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " │");
            if (choice.getVotes().size() == 0) {
                for (int i = 0; i < 12; i++) {
                    current.append(Emojis.CHART_EMPTY);
                }
                current.append("│ (" + choice.getVotes().size() + ")\n");
            } else {
                StringBuilder local = new StringBuilder();
                float percent = (choice.getVotes().size() / totalVotes) * 12;
                for (int i = 0; i < percent; i++) local.append(Emojis.CHART_FULL);
                while (local.length() != 12) {
                    local.append(Emojis.CHART_EMPTY);
                }
                current.append(local).append("│ (" + choice.getVotes().size() + ")\n");
            }
        });

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Umfrage: " + poll.getName())
                .setDescription(poll.getDescription())
                .addField("Möglichkeiten:", choices.toString(), false);
        if (poll.areVotesVisible())
            builder.addField("Jetziger Stand: (" + (int) totalVotes + " Stimme" + (totalVotes == 1 ? "" : "n") + ")", current.toString(), false);
        builder.addField("Hinweise:", "Reagiere mit den entsprechenden Buchstaben " +
                        "um für eine Möglichkeit abzustimmen. Du hast **" + poll.getVotesPerUser() + "** " +
                        (poll.getVotesPerUser() == 1 ? "Stimme\n" : "Stimmen.\n") +
                        "Wenn du deine Auswahl zurücksetzen willst, reagiere mit " + Emojis.CLOSE + ". " +
                        "Um deine jetzige Auswahl zu erhalten, reagiere mit " + Emojis.INFO + "\n" +
                        "Du musst seit mindestens zwei Wochen auf diesem Server sein, damit Umfragen nicht manipuliert werden können.\n\n" +
                        "Der Ersteller einer Umfrage kann diese mit dem Reagieren mit " + Emojis.LOCK + " schließen.", true)
                .setFooter("Diese Umfrage ist bis " + poll.getCloseDisplay() + " geöffnet.",
                        DiscordBot.INSTANCE.jda.getUserById(poll.getPollOwner()).getAvatarUrl());
        return builder.build();
    }

    private MessageEmbed createPollResultMessage(Poll poll) {
        StringBuilder choices = new StringBuilder();
        StringBuilder current = new StringBuilder();
        float totalVotes = poll.getChoices().stream().map(PollChoice::getVotes).collect(Collectors.summingInt(List::size));

        poll.getChoices().forEach(choice -> {
            choices.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " | " + choice.getText() + "\n");
        });

        poll.getChoices().forEach(choice -> {
            current.append(Emojis.EMOJI_LETTERS.get(choice.getChoiceId()) + " │");
            if (choice.getVotes().size() == 0) {
                for (int i = 0; i < 12; i++) {
                    current.append(Emojis.CHART_EMPTY);
                }
                current.append("│ (" + choice.getVotes().size() + ")\n");
            } else {
                StringBuilder local = new StringBuilder();
                float percent = (choice.getVotes().size() / totalVotes) * 12;
                for (int i = 0; i < percent; i++) local.append(Emojis.CHART_FULL);
                while (local.length() != 12) {
                    local.append(Emojis.CHART_EMPTY);
                }
                current.append(local).append("│ (" + choice.getVotes().size() + ")\n");
            }
        });
        return new EmbedBuilder()
                .setTitle("Umfrageergebnis: " + poll.getName())
                .setDescription(poll.getDescription())
                .addField("Möglichkeiten:", choices.toString(), false)
                .addField("Ergebnis: (" + (int) totalVotes + " Vote" + (totalVotes == 1 ? "" : "s") + ")", current.toString(), false)
                .setFooter("Geschlossen seit " + poll.getCloseDisplay(),
                        DiscordBot.INSTANCE.jda.getUserById(poll.getPollOwner()).getAvatarUrl())
                .build();
    }

    private void createPoll(PollSetup data) throws SQLException {
        setups.remove(data.userId);
        if (!isPollDataReady(data)) {
            DiscordBot.INSTANCE.jda.getTextChannelById(data.channelId).sendMessage(MsgCreator.of(new EmbedBuilder()
                    .setTitle("Nicht genügend Informationen")
                    .setDescription("Eine Umfrage benötigt:\n```" +
                            "- einen Namen\n" +
                            "- eine Beschreibung\n" +
                            "- einen Endzeitpunkt\n" +
                            "- mindestens zwei Antortmöglichkeiten```")
                    .setFooter("Diese Nachricht wird in 5 Sekunden gelöscht.")
            )).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        setups.remove(data.userId);
        Message msg = data.targetChannel.sendMessage(MsgCreator.of(
                        new EmbedBuilder().setDescription("Diese Nachricht wird gleich durch eine Umfrage ersetzt.")))
                .complete();
        List<String> emojis = new ArrayList<>();
        for (int i = 0; i < data.choices.size(); i++) {
            emojis.add(Emojis.EMOJI_LETTERS.get(i));
        }
        emojis.add(Emojis.INFO);
        emojis.add(Emojis.CLOSE);

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
                return msg.getId();
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

            @Override
            public boolean areVotesVisible() {
                return data.showVotes;
            }
        };
        data.msg.editMessage(MsgCreator.of(new EmbedBuilder().setTitle("Umfrage erstellt.")
                        .setDescription("Die Umfrage wurde erfolgreich erstellt und im Channel "
                                + data.targetChannel.getAsMention() + " veröffentlicht.")
                ))
                .queue();

        data.msg.clearReactions().queue();
        msg.editMessage(MsgCreator.of(createPollMessage(poll))).queue();
        new Emojis.ReactionAdder(emojis).addReactions(msg, () -> {
            msg.addReaction(Emojis.LOCK);
        });
        logPoll(poll, msg.getGuild(), false, false, false);
        database.savePoll(poll);
        database.loadAllPolls();
    }

    private boolean isPollDataReady(PollSetup data) {
        return data.name != null && data.description != null && data.choices.size() > 1 && data.endTime != null && data.targetChannel != null;
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
