package main.java.util;

import java.util.List;

public interface Poll {

    /**
     * Returns the name of the poll (this is what is displayed)
     *
     * @return poll name
     */
    String getName();

    /**
     * Returns the description of the poll
     *
     * @return poll description
     */
    String getDescription();

    /**
     * Returns the choices with the corresponding votes
     *
     * @return a map with key: Choice, value: userId
     */
    List<PollChoice> getChoices();

    /**
     * Get the guild id of the corresponding poll
     *
     * @return guild id
     */
    String getGuildId();

    /**
     * Get the message id of the message where the poll is visible
     *
     * @return message id or null if not visible
     */
    String getMessageId();

    /**
     * Get the number of votes per user
     *
     * @return votes per user
     */
    int getVotesPerUser();

    /**
     * Get the time as unix timestamp when the poll will close
     *
     * @return unix timestamp
     */
    long getCloseTime();

    /**
     * Returns a formatted date string of getCloseTime()
     *
     * @return formatted date string
     */
    String getCloseDisplay();

    /**
     * Returns the creator of the poll
     *
     * @return id of the poll creator
     */
    String getPollOwner();

    /**
     * Returns If votes should be displayed
     * @return display votes?
     */
    boolean areVotesVisible();
}
