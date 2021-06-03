package main.java.commands.server.pmCommands;

import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.UnbanHandlerDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.UnbanHandlerDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class UnbanRequestHandler {

    private static final HashMap<Long, UnbanRequest> users = new HashMap<>();

    public static void handle(MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay();
        ChannelDatabaseSQLite channelDatebase = new ChannelDatabaseSQLite();
        UserRecordsDatabaseSQLite userRecordsDatabase = new UserRecordsDatabaseSQLite();
        UnbanHandlerDatabase unbanHandlerDatabase = new UnbanHandlerDatabaseSQLite();

        PrivateChannel channel = event.getPrivateChannel();
        User user = event.getPrivateChannel().getUser();
        long userId = user.getIdLong();

        if (!users.containsKey(userId)) {
            if (message.startsWith("%unban")) {
                users.put(userId, new UnbanRequest(userId));
                channel.sendMessage("Für welchen Server möchtest du einen Entbannungsantrag stellen? ```ServerID``` \n \n "
                        + ":rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
            } else {
                channel.sendMessage("Wenn du einen Entbannungsantrag stellen möchtest beginne mit ```%unban``` \n "
                        + "Um den Vorgang abzubrechen nutze ```exit``` \n \n :rotating_light: **Achtung!:** du kannst nur einen Antrag je Server stellen").queue();
            }
            return;
        }

        UnbanRequest request = users.get(userId);
        if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("cancel")) {
            users.remove(userId);
            channel.sendMessage("**Vorgang abgebrochen**").queue();
            return;
        }
        if (request.getGuildID() == 0) {
            // überprüfen ob Serverexistiert/ gebannt ist
            if (message.length() == 18) {
                try {
                    Guild guild = event.getJDA().getGuildById(message);
                    if (guild == null) {
                        channel.sendMessage("Der Server konnte nicht gefunden werden, bitte überprüfe die ID.").queue();
                        return;
                    }
                    //check if user already committed a request on the serverId
                    //if (new UserRecords().isUnbanRequest(guild.getIdLong(), userId)) {
                    if (userRecordsDatabase.isUnbanRequest(guild.getIdLong(), userId)) {
                        channel.sendMessage("Du hast auf diesen Server bereits einen Antrag gestellt.").queue();
                        users.remove(userId);
                        return;
                    }
                    try {
                        guild.retrieveBanById(userId).queue(null, new ErrorHandler()
                                .handle(
                                        ErrorResponse.UNKNOWN_BAN, (e) -> {
                                            channel.sendMessage("**Du bist auf diesem Server nicht gebannt.**").queue();
                                            users.remove(userId);
                                        }));

                        long serverID = guild.getIdLong();
                        request.setGuildID(serverID);

                        channel.sendMessage("Bitte begründe warum du möchtest, dass dein Bann aufgehoben wird.").queue();

                    } catch (InsufficientPermissionException e) {
                        TextChannel audit = channelDatebase.getAuditChannel(guild);
                        audit.sendMessage(":no_entry_sign: Wegen fehlenden Berechtigungen konnte ein Entbannungsantrag nicht bearbeitet werden. "
                                + "Der Bot benötigt die Berechtigung: ban_members").queue();
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

            users.remove(userId);

            userRecordsDatabase.addRecord(userId, System.currentTimeMillis(), 0, "unbanrequest", request.getGuildID(), null, null);

            Guild guild = event.getJDA().getGuildById(request.getGuildID());
            String reason = request.getReason();
            TextChannel audit = null;

            if (channelDatebase.getPmChannel(guild) != null) {
                audit = channelDatebase.getPmChannel(guild);
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
            b.setDescription("**Nutzer:** " + channel.getUser().getName() + "(" + userId + ") \n \n"
                    + "**Begründung:** " + reason + "\n \n **Gebannt wegen:** " + guild.retrieveBanById(userId).complete().getReason());

            long auditChannelId = audit.getIdLong();

            try {
                audit.sendMessage(b.build()).queue(m -> {
                    m.addReaction("U+2705").queue();
                    m.addReaction("U+274C").queue();
                    unbanHandlerDatabase.addVoteReactions(guild.getIdLong(), auditChannelId, m.getIdLong(), channel.getUser().getIdLong());
                });
            } catch (IllegalArgumentException | NullPointerException e) {
                //
            }
        }
    }
}
