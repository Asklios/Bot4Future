package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Member;

/**
 * Interface for requests to the invitemanager table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public interface InviteDatabase {

    /**
     * Saves the specialMember and the newly verified.
     * @param specialMember the member using the verify command.
     * @param verifiedMember the member that was verified.
     */
    void saveVerified(Member specialMember, Member verifiedMember);

    /**
     * Saves the new specialMember.
     * @param specialMember the member using the special invite code.
     */
    void saveSpecialMember(Member specialMember);

    /**
     * Deletes all entries from the provided guild.
     * @param guildId the id of the requesting guild.
     */
    void deleteGuildData(long guildId);
}
