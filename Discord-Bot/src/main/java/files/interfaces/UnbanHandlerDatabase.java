package main.java.files.interfaces;

public interface UnbanHandlerDatabase {
    void addVoteReactions(long guildId, long channelId, long messageId, long bannedId);

    void removeGuildData(long guildId);
}
