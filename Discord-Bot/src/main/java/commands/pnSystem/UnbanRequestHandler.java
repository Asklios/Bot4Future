package main.java.commands.pnSystem;

import main.java.files.LiteSQL;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class UnbanRequestHandler {

    private static HashMap<Long, UnbanRequest> users = new HashMap<>();

    public static void handle(MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay();
        ChannelDatabaseSQLite channelDatebase = new ChannelDatabaseSQLite();
        UserRecordsDatabaseSQLite userRecordsDatabase = new UserRecordsDatabaseSQLite();

        PrivateChannel channel = event.getPrivateChannel();
        User user = event.getPrivateChannel().getUser();
        long userID = user.getIdLong();

        if (!users.containsKey(userID)) {

            if (message.startsWith("%unban")) {
                users.put(userID, new UnbanRequest(userID));
                channel.sendMessage("Für welchen Server möchtest du einen Entbannungsantrag stellen? ```ServerID``` \n \n "
                        + ":rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
            } else {
                channel.sendMessage("Wenn du einen Entbannungsantrag stellen möchtest beginne mit ```%unban``` \n "
                        + "Um den Vorgang abzubrechen nutze ```exit``` \n \n :rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
            }
        } else {
            UnbanRequest request = users.get(userID);
            if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("cancel")) {
                users.remove(userID);
                channel.sendMessage("**Vorgang abgebrochen**").queue();
                return;
            }
            if (request.getGuildID() == 0) {
                // überprüfen ob Serverexistiert/ gebannt ist
                if (message.length() == 18) {
                    try {
                        Guild guild = event.getJDA().getGuildById(message);
                        if (!guild.equals(null)) {
                            //check if user already committed a request on the serverId
                            if (!userRecordsDatabase.unbanRequestValue(userID, guild.getIdLong())) {
                                try {
                                    guild.retrieveBanById(userID).queue(null, new ErrorHandler()
                                            .handle(
                                                    ErrorResponse.UNKNOWN_BAN, (e) -> {
                                                        channel.sendMessage("**Du bist auf diesem Server nicht gebannt.**").queue();
                                                        users.remove(userID);
                                                    }));

                                    long serverID = guild.getIdLong();
                                    request.setGuildID(serverID);

                                    channel.sendMessage("Bitte begründe warum du möchtest, dass dein Bann aufgehoben wird.").queue();

                                } catch (InsufficientPermissionException e) {
                                    TextChannel audit = channelDatebase.getAuditChannel(guild);
                                    audit.sendMessage(":no_entry_sign: Wegen fehlenden Berechtigungen konnte ein Entbannungsantrag nicht bearbeitet werden. "
                                            + "Der Bot benötigt die Berechtigung: ban_members").queue();
                                }
                            } else {
                                channel.sendMessage("Du hast auf diesen Server bereits einen Antrag gestellt.").queue();
                                users.remove(userID);
                                return;
                            }
                        } else {
                            channel.sendMessage("Der Server konnte nicht gefunden werden, bitte überprüfe die ID.").queue();
                        }
                    } catch (NumberFormatException e) {
                        channel.sendMessage("**Falsche Formatierung:** \n Die Server-Id ist eine 18-stellige Nummer, du findest sie in der Bannbenachrichtigung").queue();
                    }
                }
            } else if (request.getReason() == null) {
                if (message.length() < 1500) {
                    request.setReason(message);
                } else {
                    channel.sendMessage("Bitte beschränke dich auf 1500 Zeichen.").queue();
                    return;
                }

                users.remove(userID);

                userRecordsDatabase.addRecord(userID, System.currentTimeMillis(), 0, "unbanrequest", request.getGuildID(), null, null);

                Guild guild = event.getJDA().getGuildById(request.getGuildID());
                String reason = request.getReason();
                TextChannel audit = null;

                if (channelDatebase.getPnChannel(guild) != null) {
                    audit = channelDatebase.getPnChannel(guild);
                    channel.sendMessage("Dein Entbannungsantrag wurde an die Verantwortlichen gesendet").queue();
                } else if (channelDatebase.getAuditChannel(guild) != null) {
                    audit = channelDatebase.getAuditChannel(guild);
                    channel.sendMessage("Dein Entbannungsantrag wurde an die Verantwortlichen gesendet").queue();
                } else {
                    channel.sendMessage("Es konnte keine Nachricht gesendet werden, da der Server die Funktion nicht aktiviert hat.").queue();
                }

                EmbedBuilder b = new EmbedBuilder();
                b.setColor(0xff00ff); //helles Lila
                b.setTitle(":innocent: Entbannungsantrag");
                b.setTimestamp(OffsetDateTime.now());
                b.setThumbnail(channel.getUser().getAvatarUrl() == null ? channel.getUser().getDefaultAvatarUrl() : channel.getUser().getAvatarUrl());
                b.setDescription("**Nutzer:** " + channel.getUser().getName() + "(" + userID + ") \n \n"
                        + "**Begründung:** " + reason + "\n \n **Gebannt wegen:** " + guild.retrieveBanById(userID).complete().getReason());

                long auditMessageId = 0;

                try {
                    auditMessageId = audit.sendMessage(b.build()).complete().getIdLong();
                } catch (IllegalArgumentException | NullPointerException e) {

                }

                //reactions
                LiteSQL.onUpdate("INSERT INTO unbanhandlerreactions(guildid, channelid, messageid, bannedid) VALUES(" +
                        guild.getIdLong() + ", " + audit.getIdLong() + ", " + auditMessageId + ", " + channel.getUser().getIdLong() + ")");

                audit.retrieveMessageById(auditMessageId).queue(a -> {
                    a.addReaction("U+2705").queue();
                    a.addReaction("U+274C").queue();
                });

            }
        }
    }
}
