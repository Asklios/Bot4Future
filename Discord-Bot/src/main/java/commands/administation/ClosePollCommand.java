package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.LiteSQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.concurrent.TimeUnit;

public class ClosePollCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            return;
        }

        // %closepoll #channel <MessageId>
        Guild guild = channel.getGuild();
        long guildID = guild.getIdLong();
        long channelID = channel.getIdLong();
        String[] messageSplit = message.getContentDisplay().split("\\s+");
        long textChannelId = message.getMentionedChannels().get(0).getIdLong();
        String messageIdString = messageSplit[2];
        long messageID;

        try {
            guild.getTextChannelById(textChannelId).retrieveMessageById(messageIdString).complete();
        } catch (IllegalArgumentException | InsufficientPermissionException e) {
            channel.sendMessage("Die Nachricht konnte nicht gefunden werden oder der Bot hat keine Berechtigung sie zu lesen").complete().delete().queueAfter(5, TimeUnit.SECONDS);
            return;
        }
        messageID = Long.parseLong(messageIdString);

        LiteSQL.onUpdate("DELETE FROM votereactions WHERE guildid = " + guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);

        channel.sendMessage("Die Abstimmung mit der ID: " + messageID + " wurde geschlossen.").queue();
    }
}
