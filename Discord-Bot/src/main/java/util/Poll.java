package main.java.util;

import java.util.List;

public interface Poll {

    /**
     * Returns the poll id (internal id)
     *
     * @return poll id
     */
    long getPollId();

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
     * @return guild id
     */
    Long getGuildId();

    /**
     * Get the message id of the message where the poll is visible
     * @return message id or null if not visible
     */
    String getMessageId();

}
