package main.java.files.interfaces;

import main.java.util.Poll;

import java.util.List;

public interface PollDatabase {
    /**
     * Load all polls in the ram
     */
    void loadAllPolls();
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
    Poll getPoll(Long id);

    /**
     * Delete poll by id
     * @param id poll id
     */
    void deletePoll(Long id);

    /**
     * Stores a poll in the database
     * @param poll poll to store
     */
    void savePoll(Poll poll);
}
