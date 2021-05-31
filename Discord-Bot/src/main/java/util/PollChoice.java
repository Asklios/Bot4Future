package main.java.util;

import java.util.List;

public interface PollChoice {
    /**
     * Get internal choice id
     * @return choice id
     */
    long getChoiceId();

    /**
     * Get internal poll id of the choice
     * @return poll id
     */
    long getPollId();

    /**
     * Get all votes (all userids of the users that picked that choice)
     * @return
     */
    List<Long> getVotes();

    /**
     * Get the displayed text of that choice
     * @return text
     */
    String getText();
}
