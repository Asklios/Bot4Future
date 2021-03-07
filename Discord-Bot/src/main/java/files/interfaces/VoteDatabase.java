package main.java.files.interfaces;

public interface VoteDatabase {

    void addVote(long guildID, long channelID, long messageID, long userID, String emote) throws Exception;
    void removeVote(long guildID, long channelID, long messageID, long userID, String emote) throws Exception;

    boolean hasVoted(long guildID, long channelID, long messageID, long userID);
    boolean isEmote(long guildID, long channelID, long messageID, String emote);

    void removeGuildData(long guildId);
}

