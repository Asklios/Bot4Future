package main.java.commands.server.administation.poll;

import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class PollCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
/*
        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("Du hast nicht die nötige Berechtigung um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        Guild guild = channel.getGuild();

        //poll <emote1> % <text1> % <emote2> % <text2> % <emote3> % <text3>
        String[] messageSplit = message.getContentRaw().substring(5).split("%");

        if (messageSplit.length < 2) {
            channel.sendMessage("```%poll <emote1> % <text1> % <emote2> % <text2> % <emote3> % <text3> ...```").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        EmbedBuilder b = new EmbedBuilder();

        List<String> emotes = new ArrayList<>();
        List<String> texts = new ArrayList<>();


        for (int i = 0; i < messageSplit.length; i++) {
            emotes.add(messageSplit[i]);
            i++;
            try {
                texts.add(messageSplit[i]);
            } catch (ArrayIndexOutOfBoundsException e) {
                //bei Emote ohne Text
                emotes.remove(i/2-1);
            }

        }

        if (emotes == null) {
            channel.sendMessage("Es wurden keine Emotes gefunden. ```%poll <emote1> % <text1> % <emote2> % <text2> % <emote3> % <text3> ...```").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        for (int i = 0; i < emotes.size(); i++) {
            try {
                b.appendDescription(emotes.get(i) + " - " + texts.get(i) + "\n \n");
            } catch (IndexOutOfBoundsException e) {
                b.appendDescription(emotes.get(i) + "\n \n");
            }
        }


        long messageId = channel.sendMessage(b.build()).complete().getIdLong();


        try {
            for (int i = 0; i < emotes.size(); i++) {
                String emote = emotes.get(i).replaceAll(" ", "");
                channel.addReactionById(messageId, emote).queue();
            }
        } catch (ErrorResponseException | IllegalArgumentException e) {
            channel.sendMessage("Min. ein Emote konnte nicht gefunden werden. Es sind nur Unicode-Emotes erlaubt. ```%poll <emote1> % <text1> % <emote2> % <text2> % <emote3> % <text3> ...```").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            channel.deleteMessageById(messageId).queue();
            return;
        }

        // save to .db
        long guildId = guild.getIdLong();
        long channelId = channel.getIdLong();
        String emoteString = "";
        String valueString = "";
        for (String e : emotes) {
            emoteString += e + "⫠";
            valueString += "0⫠";
        }
        String textString = "";
        for (String t : texts) {
            textString += t + "⫠";
        }

        LiteSQL.onUpdate("INSERT INTO votereactions(guildid, channelid, messageid, emotes, texts, value) VALUES (" + guildId + ", " + channelId + ", "
                + messageId + ", '" + emoteString + "', '" + textString + "', '" + valueString + "')");*/
    }
}