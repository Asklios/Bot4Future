package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;

/**
 * Interface for requests to the calldata table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public interface CallDatabase {

    /**
     * Checks if the call-name is already taken.
     * @param guildId the id of the current guild.
     * @param name the user provided call-name.
     * @return returns true if the name does not exist yet, otherwise false.
     */
    boolean checkName(long guildId, String name);

    /**
     * Saves the collected presence data to the database an returns the entry id.
     * @param guild the id of the current guild.
     * @param users a String of the participating userIds.
     * @param startTime the startTime of the call in milliseconds.
     * @param endTime the endTime of the call in milliseconds.
     * @param name the user provided call-name.
     * @param requester the userId from the command user.
     * @return returns the entry id from the database, 0 if there has been an exception.
     */
    long saveCallData(Guild guild, String users, long startTime, long endTime, String name, long requester);

    /**
     * Can search the database for an entry by id or call-name.
     * Checks if the user has the permission ADMINISTRATOR or has requested the data collection himself.
     * @param search can be an entry id or the call-name.
     * @param channel the textChannel the request comes from.
     * @param member the member that is requesting the information.
     * @return the String of userIds that was previously saved, empty if there is no entry, null for missing permissions.
     */
    String getUsersFromDb(String search, GuildMessageChannel channel, Member member);

    /**
     * Deletes all entries that are related to the provided guild.
     * @param guildId the id of the requesting guild.
     * @return true if deleted successful, false if there were no entries.
     */
    boolean removeGuildData(long guildId);
}
