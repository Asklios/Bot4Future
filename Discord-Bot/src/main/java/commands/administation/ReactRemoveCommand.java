package main.java.commands.administation;

import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReactRemoveCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
        // %unreact #channel <MessageID> :emote: :emote2: :emote3:
        // messageText[1] messageText[2]  messageText[3]    ...

        if (member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {

            String[] messageText = message.getContentDisplay().split("\\s+");
            List<TextChannel> channels = message.getMentionedChannels();

            if (!channels.isEmpty() && messageText.length >= 4) {
                TextChannel textChannel = message.getMentionedChannels().get(0);
                String messageIDString = messageText[2];

                try {
                    Long messageID = Long.parseLong(messageIDString);

                    for (int i = 3; i < messageText.length; i++) {


                        if (!messageText[i].equals("")) {
                            channel.sendTyping().queue(); // wird 10sek lang gesendet
                            textChannel.clearReactionsById(messageID, messageText[i]).queue(null,
                                    new ErrorHandler().handle(ErrorResponse.UNKNOWN_EMOJI, (e) -> textChannel.sendMessage("unbekanntes Emote").queue())
                                            .handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> textChannel.sendMessage("unbekannte Message ID").queue())
                                            .handle(ErrorResponse.UNKNOWN_CHANNEL, (e) -> textChannel.sendMessage("unbekannter Channel").queue()));
                        } else {
                            channel.sendMessage("ungültige Formatierung").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                            return;
                        }
                    }

                } catch (NumberFormatException e) {

                    channel.sendMessage("ungültige Formatierung").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                    System.err.println("Cought exception: NumberFormatException (ReactCommand.java)");

                }

            } else {
                channel.sendMessage("Falsche Formatierung!").complete().delete().queueAfter(5, TimeUnit.SECONDS);
                EmbedBuilder builder = new EmbedBuilder();
                builder.setDescription("%react #channel <MessageID> :emote: (:emote2:) (:emote3:) (...)");
                channel.sendMessage(builder.build()).complete().delete().queueAfter(10, TimeUnit.SECONDS);
            }
        } else {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
        }
    }
}

