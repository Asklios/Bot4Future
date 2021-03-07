package main.java.helper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that finds ether a mention or a user id.
 *
 * @author Asklios
 * @version 18.11.2020
 */

public class GetMemberFromMessage {

    /**
     * Finds the first mentioned member or the first id.
     * @param message the message that should be searched.
     * @return returns the first mentioned member, if none is found it checks for ids, otherwise returns false.
     */
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

    /**
     * Finds all member ids.
     * @param searchString that should be searched for ids.
     * @param message the message. Will be used to get the guild.
     * @return a list of members.
     */
    public static List<Member> allMemberIds(String searchString, Message message) {

        Guild guild = message.getGuild();
        String[] messageSplit = searchString.split("\\s+");
        List<Member> mentions = new ArrayList<>();

        for (String s : messageSplit) {
            try {
                mentions.add(guild.retrieveMemberById(s).complete());
            } catch (IllegalArgumentException e) {
                //split is not an id
            }
        }
        return mentions;
    }

    /**
     * Finds all member mentions and ids in the original order.
     * Can be used to only check parts of a message.
     * @param searchStringRaw the raw message content that should be searched.
     * @param message the message. Will be used to get the guild.
     * @return a list of members.
     */
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
