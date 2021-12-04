package main.java.commands.server.user;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReportCommand implements ServerCommand {

    List<String> messages;
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        String[] messageSplit = message.getContentDisplay().split(" ", 3);

        //%report @Nutzer <reason>
        if (messageSplit.length > 2) {

            List<Member> members = message.getMentionedMembers();
            Member reportedMember = null;
            try {
                reportedMember = members.get(0);
            } catch (IndexOutOfBoundsException e) {
                //
            }
            User reportingUser = message.getAuthor();
            String reason = messageSplit[2];

            if (reportedMember != null && reason != null) {

                try {
                    int amount = 100;
                    writeFile(message, channel, amount, reportedMember, reportingUser, reason);

                } catch (NumberFormatException e) {
                    System.err.println("Cought exception: log amount larger than int (LogCommand.java)");
                } catch (ArrayIndexOutOfBoundsException e) {
                    channel.sendMessage("Falsche Formatierung: ```%report @Nutzer <reason>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                }
            } else {
                channel.sendMessage("Falsche Formatierung: ```%report @Nutzer <reason>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("```%report @Nutzer <reason>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }

    //Nachrichten werden abgerufen und in eine .txt geschrieben
    private void writeFile(Message originMessage, MessageChannel channel, int amount, Member reportedMember, User reportingUser, String reason) {

        String path = DiscordBot.INSTANCE.getLogFilePath();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        MessageHistory messageHistory = channel.getHistoryBefore(originMessage, amount).complete();
        List<Message> messageList = messageHistory.getRetrievedHistory();
        List<String> textList = new ArrayList<>();
        textList.add("Logfile - time created: " + OffsetDateTime.now().format(dateFormat) + " - guild: " + originMessage.getGuild().getName() + " - channel: #" + originMessage.getChannel().getName() + " - requested by: " + originMessage.getAuthor().getName() + " (" + originMessage.getAuthor().getId() + ")");
        textList.add("");
        textList.add("reported: " + reportedMember.getEffectiveName() + " (" + reportedMember.getId() + ") - reason: " + reason);
        textList.add("");

        String messageTime;
        String author;
        String messageText;

        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            messageTime = message.getTimeCreated().format(dateFormat);
            author = "(" + message.getAuthor().getId() + ")" + message.getAuthor().getName();

            messageText = message.getContentRaw();
            if (!message.getAttachments().isEmpty()) {
                if (!messageText.equals("")) {
                    messageText += " ";
                }
                messageText += "(" + message.getAttachments().get(0).getUrl() + ")";
            }
            else if (!message.getEmbeds().isEmpty()) {
                List<MessageEmbed> embeds = message.getEmbeds();
                for (MessageEmbed e : embeds) {
                    messageText += "Embed: ";
                            if (e.getTitle() != null) { messageText += "Title[" + e.getTitle() +"] ";}
                            if (e.getDescription() != null) { messageText += "Description[" + e.getDescription() +"] ";}
                            if (e.getUrl() != null) { messageText += "Url[" + e.getUrl() +"] ";}
                            if (!e.getFields().isEmpty()) {
                                List<MessageEmbed.Field> fields = e.getFields();
                                int j = 1;
                                for (MessageEmbed.Field f : fields) {
                                    messageText += "Field" + j + "{";
                                    if (f.getName() != null) {messageText += "Name[" + f.getName() + "] ";}
                                    if (f.getValue() != null) {messageText += "Value[" + f.getValue() + "] ";}
                                    messageText += "} ";
                                    j++;
                                }
                            }
                            if (e.getThumbnail() != null) { messageText += "Thumbnail[" + e.getThumbnail().getUrl() +"] ";}
                            if (e.getFooter() != null) { messageText += "Footer[" + e.getFooter().getText() +"] ";}
                }
            }

            textList.add(messageTime + " - by: " + String.format("%-" + 50 + "." + 50 + "s", author) + " - message: " + messageText);
        }

        textList.add("");
        textList.add("-- Logfile created by Bot4Future --");

        try {
            OutputStreamWriter myWriter = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);

            String text = String.join("\r\n", textList);

            myWriter.write(text);
            myWriter.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        // .txt wird in den Audit-Channel geschickt

        TextChannel audit = this.channelDatabase.getAuditChannel(originMessage.getGuild());

        EmbedBuilder embed = new EmbedBuilder();
        File file = new File(DiscordBot.INSTANCE.getLogFilePath());
        String fileName = "log-report " + OffsetDateTime.now().format(dateFormat) + " " + channel.getName() + ".txt";

        if (audit != null) {

            try {
                embed.setColor(0xff00ff); //helles Lila
                embed.setTitle(":mag: User reported");
                embed.setAuthor(originMessage.getGuild().getSelfMember().getNickname());
                embed.setTimestamp(OffsetDateTime.now());
                embed.setThumbnail(originMessage.getGuild().getSelfMember().getUser().getAvatarUrl());
                embed.setDescription(reportedMember.getAsMention() + " wurde von " + originMessage.getAuthor().getAsMention() + " reported" +
                        "\n reason: *" + reason + "*\n" +
                        "\n Das Log enthält die letzten " + amount + " Nachrichten aus <#" + channel.getId() + ">");
                audit.sendFile(file, fileName).setEmbeds(embed.build()).queue();

                channel.sendMessage(reportingUser.getAsMention() + " Die verantwortlichen Moderatoren wurden benachrichtigt, es sind keine weiteren Pings nötig.")
                        .queue(m -> m.delete().queueAfter(15,TimeUnit.SECONDS));
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (InsufficientPermissionException e) {
                channel.sendMessage(":no_entry_sign: Dem Bot fehlt die nötigen Berechtigungen um den Befehl erfolgreich auszuführen").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("Dieser Command kann nicht genutzt werden, da noch kein `#audit` Channel festgelegt wurde.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }

        // .txt wird gelöscht
        file.delete();

    }
}