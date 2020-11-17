package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.GetMemberFromMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ClearMessagesFromUserCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(")
                    .queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        User targetUser = Objects.requireNonNull(GetMemberFromMessage.firstMentionedMember(message)).getUser();

        channel.getHistoryBefore(message, 100).queue(messageHistory -> {

            List<Message> messageList = messageHistory.getRetrievedHistory();
            ArrayList<Message> userMessages = new ArrayList<>();

            for (Message m : messageList) {
                if (m.getAuthor().equals(targetUser)) {
                    userMessages.add(m);
                }
            }

            channel.deleteMessages(userMessages).queue();
        });
    }
}
