package main.java.util;

import main.java.DiscordBot;
import main.java.files.interfaces.StrikeUpdateDatabase;
import main.java.files.interfaces.SubscribtionDatabase;
import main.java.helper.api.LocalGroup;
import main.java.helper.api.LocalGroups;
import main.java.helper.api.Strike;
import main.java.helper.api.Strikes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StrikeNotifier {
    private final SubscribtionDatabase subscribtionDB;
    private final StrikeUpdateDatabase updatesDB;

    private DateTimeFormatter DATEFORMATTER = DateTimeFormat.forPattern("d.MM.yyyy");
    private DateTimeFormatter TIMEFORMATTER = DateTimeFormat.forPattern("HH:mm");

    public StrikeNotifier(SubscribtionDatabase db) {
        subscribtionDB = db;
        updatesDB = db.getUpdateDatabase();
    }

    public void handleNotify() {
        List<Strike> strikes = new Strikes().getStrikes();
        strikes.forEach(strike -> {
            //if ((strike.getDateTime().getTime() - TimeUnit.HOURS.toMillis(12) < System.currentTimeMillis())) return;
            Long lastUpdate = updatesDB.getLastHandledUpdate(strike.getId());

            LocalGroup group = new LocalGroups().getGroupByName(strike.getLocalGroupName());
            if (group == null) return;
            subscribtionDB.getSubscribtionsForLocalGroup(group.getId()).forEach(userId -> {
                if (lastUpdate == null) {
                    handleNewStrike(userId, strike, group.getId());
                } else if (lastUpdate < strike.getDateTime().getTime()) {
                    handleStrikeUpdate(userId, strike, group.getId());
                }
                updatesDB.setLastHandledUpdate(strike.getId(), strike.getLastUpdate().getTime());
            });

        });
    }

    private void handleStrikeUpdate(String userId, Strike strike, Long ogId) {
        User user = DiscordBot.INSTANCE.jda.getUserById(userId);
        if (user == null) {
            System.out.println("User " + userId + " nicht verfügbar! Deaboniere...");
            subscribtionDB.unsubscribe(userId, ogId);
            return;
        }
        user.openPrivateChannel()
                .flatMap(channel -> {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(new Color(27, 115, 64));
                    if (strike.getEventLink() != null)
                        builder.setTitle("Streikupdate: "
                                + strike.getLocationName()
                                + " am "
                                + DATEFORMATTER.print(strike.getDateTime().getTime()), strike.getEventLink());
                    else
                        builder.setTitle("Streikupdate: "
                                + strike.getLocationName()
                                + " am "
                                + DATEFORMATTER.print(strike.getDateTime().getTime()));

                    builder.addField("Ortsgruppe", strike.getLocalGroupName(), false)
                            .addField("Ort", strike.getLocationName(), false)
                            .addField("Bundesland", strike.getState(), false)
                            .addField("Start", TIMEFORMATTER.print(strike.getDateTime().getTime()), false);
                    if (strike.getNote() != null)
                        builder.addField("Hinweis", strike.getNote(), false);
                    builder.setFooter("Letztes Update: " + DiscordBot.FORMATTER.print(strike.getLastUpdate().getTime()));
                    builder.setImage("https://dl.dropboxusercontent.com/s/zyhcme622yf8x88/4096-4096-max.jpg?dl=0");
                    return channel.sendMessage(builder.build());
                }).queue();
    }

    private void handleNewStrike(String userId, Strike strike, Long ogId) {
        User user = DiscordBot.INSTANCE.jda.getUserById(userId);
        if (user == null) {
            System.out.println("User " + userId + " nicht verfügbar! Deaboniere...");
            subscribtionDB.unsubscribe(userId, ogId);
            return;
        }
        user.openPrivateChannel()
                .flatMap(channel -> {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setColor(new Color(29, 166, 74));
                    if (strike.getEventLink() != null)
                        builder.setTitle("Neuer Streiktermin: "
                                + strike.getLocationName()
                                + " am "
                                + DATEFORMATTER.print(strike.getDateTime().getTime()), strike.getEventLink());
                    else
                        builder.setTitle("Neuer Streiktermin: "
                                + strike.getLocationName()
                                + " am "
                                + DATEFORMATTER.print(strike.getDateTime().getTime()));

                    builder.addField("Ortsgruppe", strike.getLocalGroupName(), false)
                            .addField("Ort", strike.getLocationName(), false)
                            .addField("Bundesland", strike.getState(), false)
                            .addField("Start", TIMEFORMATTER.print(strike.getDateTime().getTime()), false);
                    if (strike.getNote() != null)
                        builder.addField("Hinweis", strike.getNote(), false);
                    builder.setFooter("Sollte sich etwas an diesen Infos ändern, senden wir dir eine weitere Nachricht.");
                    builder.setImage("https://dl.dropboxusercontent.com/s/zyhcme622yf8x88/4096-4096-max.jpg?dl=0");
                    return channel.sendMessage(builder.build());
                }).queue();
    }
}
