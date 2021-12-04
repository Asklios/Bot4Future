package main.java.commands.server.administation;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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

public class LogCommand implements ServerCommand {

    List<String> messages;
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String[] messageSplit = message.getContentDisplay().split("\\s+");

        //%log 15
        if (messageSplit.length >= 2) {
            try {
                int amount = Integer.parseInt(messageSplit[1]);
                if (amount > 0) {
                    if (amount <= 100) {
                        channel.sendTyping().queue();
                        writeFile(message, channel, amount);
                    } else {
                        channel.sendMessage("Sagmal soll ich jetzt Romane schreiben?!?! ```Nachrichtenlimit: 100```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
                    }
                } else if (amount == 0) {
                    channel.sendMessage("Stell Dir vor, Du hast null Kekse und verteilst sie " +
                            "gleichmäßig an null Freunde. Siehst Du? Das macht keinen Sinn. Und das Krümelmonster ist traurig, " +
                            "weil keine Kekse mehr da sind und Du bist traurig, weil Du keine Freunde hast.").queue(m -> m.delete().queueAfter(25,TimeUnit.SECONDS));
                } else {
                    channel.sendMessage("Zukunftsvorhersage in 5, 4, 3, $%*, $%& - **critical ERROR occurred**").queue(m -> m.delete().queueAfter(12,TimeUnit.SECONDS));
                }

            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                channel.sendMessage("Falsche Formatierung: ```%log <Anzahl>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("Falsche Formatierung: ```%log <Anzahl>```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }



    }

    //Nachrichten werden abgerufen und in eine .txt geschrieben
    private void writeFile(Message originMessage, MessageChannel channel, int amount) {

        String path = DiscordBot.INSTANCE.getLogFilePath();

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        MessageHistory messageHistory = channel.getHistoryBefore(originMessage, amount).complete();
        List<Message> messageList = messageHistory.getRetrievedHistory();
        List<String> textList = new ArrayList<>();
        textList.add("Logfile - time created: " + OffsetDateTime.now().format(dateFormat) + " - guild: " + originMessage.getGuild().getName() + " - channel: #" + originMessage.getChannel().getName() + " - requested by: " + originMessage.getAuthor().getName());
        textList.add("");

        String messageTime = "time";
        String author = "author";
        String messageText = "text";

        for (int i = messageList.size() - 1; i >= 0; i--) {
            Message message = messageList.get(i);
            messageTime = message.getTimeCreated().format(dateFormat).toString();
            author = "(" + message.getAuthor().getId() + ")" + message.getAuthor().getName().toString();

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
        String fileName = "log " + OffsetDateTime.now().format(dateFormat) + " " + channel.getName() + ".txt";

        if (audit != null) {

            try {
                embed.setColor(0xcc880); //orange-braun
                embed.setTitle(":scroll: Logfile created");
                embed.setAuthor(originMessage.getGuild().getSelfMember().getNickname());
                embed.setTimestamp(OffsetDateTime.now());
                embed.setThumbnail(originMessage.getGuild().getSelfMember().getUser().getAvatarUrl());
                embed.setDescription("Log - requested by " + originMessage.getAuthor().getAsMention() + "\n enthält die letzten " + amount + " Nachrichten aus <#" + channel.getId() + ">");
                audit.sendFile(file, fileName).setEmbeds(embed.build()).queue();

                channel.sendMessage(originMessage.getAuthor().getAsMention() + " Es wurde ein Log-File in den Audit-Channel geschickt").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (InsufficientPermissionException e) {
                channel.sendMessage("Dem Bot fehlt die Berechtigung das LogFile zu senden. ```Benötigt Berechtigungen: message_read, message_write, message_attach_files```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            } catch (ErrorResponseException e) {
                //message deleted
            }
        } else {
            channel.sendMessage("Es ist keine #audit Channel festgelegt in welchen das Log geschickt werden könnte.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }

        // .txt wird gelöscht
        file.delete();
    }
}
