package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Class for requests to the guildchannels table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public interface ChannelDatabase {

    /**
     * Adds empty entries for every guild. Enables SQL: UPDATE
     * @param guild the id of the new guild.
     */
    void startUpEntries(Guild guild) throws NullPointerException;

    /**
     * Saves the TextChannel for audit-messages.
     * @param textChannel the specified audit-channel.
     */
    void saveAuditChannel (TextChannel textChannel) throws NullPointerException;
    /**
     * Returns the saved TextChannel for audit-messages.
     * @param guild the requesting guild.
     * @return The audit TextChannel or null if it is not yet defined.
     */
    TextChannel getAuditChannel (Guild guild);

    /**
     * Saves the TextChannel for Messages from the pmSystem.
     * @param textChannel the specified pm-channel.
     */
    void savePmChannel(TextChannel textChannel) throws NullPointerException;
    /**
     * Returns the saved TextChannel for the pmSystem.
     * @param guild the requesting guild.
     * @return The pm TextChannel or null if it is not yet defined.
     */
    TextChannel getPmChannel(Guild guild);

    /**
     * Sets all entries back to null for the provided guild.
     * @param guildId the id of the requesting guild.
     */
    void removeGuildData(long guildId);
}
