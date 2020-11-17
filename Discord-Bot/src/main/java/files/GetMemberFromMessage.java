package main.java.files;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class GetMemberFromMessage {

    public static Member firstMentionedMember(Message message) {

        Guild guild = message.getGuild();
        String[] messageSplit = message.getContentDisplay().split("\\s+");
        Member mention;

        if (messageSplit.length < 1) {
            return null;
        }

        try {
            mention = message.getMentionedMembers().get(0);
        } catch (IndexOutOfBoundsException e) {
            //sucht id wenn kein User erwähnt wird
            try {
                mention = guild.retrieveMemberById(messageSplit[1]).complete();
            } catch (NullPointerException | NumberFormatException f) {
                return null;
            }
        }
        return mention;
    }

    public static List<Member> allMemberIds(String searchString, Message message) {

        Guild guild = message.getGuild();
        String[] messageSplit = searchString.split("\\s+");
        List<Member> mentions = new ArrayList<>();

        for (String s : messageSplit) {
            try {
                mentions.add(guild.retrieveMemberById(s).complete());
            } catch (IllegalArgumentException e) {
                //split ist keine Id
            }
        }
        return mentions;
    }

    public static List<Member> allMemberMentionsAndIds(String searchStringRaw, Message message) {

        Guild guild = message.getGuild();
        String[] messageSplit = searchStringRaw.split("\\s+");
        List<Member> mentions = new ArrayList<>();

        for (String s : messageSplit) {
            try {
                mentions.add(guild.retrieveMemberById(s).complete());
            } catch (IllegalArgumentException e) {
                try {
                    String mention = s.replaceAll("<", "").replaceAll("@", "")
                            .replaceAll("!", "").replaceAll(">", "");
                    mentions.add(guild.retrieveMemberById(mention).complete());
                } catch (IllegalArgumentException f) {
                    //split String ist keine Id
                }
            }
        }
        return mentions;
    }
}
