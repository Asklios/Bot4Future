package main.java.files.interfaces;

import lombok.Getter;
import main.java.util.Poll;

import java.sql.SQLException;
import java.util.List;

public interface PollDatabase {
    /**
     * Load all polls in the ram
     */
    void loadAllPolls() throws SQLException;
    /**
     * Get all polls
     * @return list with all polls
     */
    List<Poll> getPolls();

    /**
     * Get poll by id
     * @param id poll id
     * @return poll with corresponding id or null
     */
    Poll getPoll(String id);

    /**
     * Delete poll by id
     * @param id poll id
     */
    void deletePoll(String id);

    /**
     * Stores a poll in the database
     * @param poll poll to store
     */
    void savePoll(Poll poll) throws SQLException;

    /**
     * Save all votes
     */
    void saveVotes() throws SQLException;

    /**
     * Gives the content of the voter list of a poll
     * @param guildId
     * @param messageId
     * @return
     */
    PollVoterList getPollVoters(String guildId, String messageId);

    /**
     * Sets the content of the voter list for the given poll.
     * @param poll
     * @param content
     */
    void setPollVoters(Poll poll, String content);

    class PollVoterList {
        @Getter
        private final String guildId;
        @Getter
        private final String msgId;
        @Getter
        private final String pollOwner;
        @Getter
        private final String content;

        public PollVoterList(String guildId, String msgId, String pollOwner, String content){
            this.content = content;
            this.msgId = msgId;
            this.pollOwner = pollOwner;
            this.guildId = guildId;
        }
    }
}
