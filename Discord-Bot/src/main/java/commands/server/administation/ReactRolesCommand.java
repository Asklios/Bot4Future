package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.LiteSQL;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReactRolesCommand implements ServerCommand {

    boolean isEmote = true;

    @Override
    public synchronized void performCommand(Member member, GuildMessageChannel channel, Message message) {

        // %reactionrole #channel <MessageID> <Emote> @Rolle
        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        String[] messageText = message.getContentDisplay().split("\\s+");

        if (messageText.length != 5) {
            channel.sendMessage("```%reactionrole #channel <MessageID> <Emote> @Rolle```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            return;
        }

        List<TextChannel> channels = message.getMentionedChannels();
        List<Role> roles = message.getMentionedRoles();
        Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);

        if (!channels.isEmpty() && !roles.isEmpty()) {

            TextChannel tc = channels.get(0);
            Role role = roles.get(0);
            String messageIDString = messageText[2];
            if (highestBotRole.canInteract(role)) {
                try {

                    long messageID = Long.parseLong(messageIDString);

                    String emote = messageText[3];
                    tc.addReactionById(messageID, emote).queue(null,
                            new ErrorHandler().handle(ErrorResponse.UNKNOWN_EMOJI, (e) ->
                                    isEmote = false
                            ));

                    if (!isEmote) {
                        channel.sendMessage("Kein gültiges Emote gefunden. `%reactionrole #channel <MessageID> <Emote> @Rolle`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                        return;
                    }

                    LiteSQL.onUpdate("INSERT INTO reactroles(guildid, channelid, messageid, emote, roleid) VALUES(" +
                            channel.getGuild().getIdLong() + ", " + tc.getIdLong() + ", " + messageID + ", '" + emote + "', " + role.getIdLong() + ")");

                } catch (NumberFormatException e) {
                    //
                }
            } else {
                channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
            }
        } else if (channels.isEmpty()) {
            channel.sendMessage("Es wurde kein TextChannel erwähnt \n ```%reactionrole #channel <MessageID> <Emote> @Rolle```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        } else {
            channel.sendMessage("Es wurde keine Rolle erwähnt \n ```%reactionrole #channel <MessageID> <Emote> @Rolle```").queue(m -> m.delete().queueAfter(10,TimeUnit.SECONDS));
        }
    }
}
