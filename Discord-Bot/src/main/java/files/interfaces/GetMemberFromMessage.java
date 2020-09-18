package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface GetMemberFromMessage {

    Member firstMentionedMember(Message message);
    List<Member> allMemberIds(String searchString, Message message);
    List<Member> allMemberMentionsAndIds(String searchStringRaw, Message message);
}
