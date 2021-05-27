package main.java.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PollChoice extends Comparable<PollChoice> {
    /**
     * Get internal choice id
     *
     * @return choice id
     */
    int getChoiceId();


    /**
     * Get all votes (all userids of the users that picked that choice)
     *
     * @return
     */
    List<String> getVotes();

    /**
     * Get the displayed text of that choice
     *
     * @return text
     */
    String getText();

    @Override
    default int compareTo(@NotNull PollChoice other){
        if(getChoiceId() < other.getChoiceId()) return -1;
        if(getChoiceId() > other.getChoiceId()) return 1;
        return 0;
    }
}
