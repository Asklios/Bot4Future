package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public interface RoleDatabase {

    /**
     * Checks if there are lines for this guild in the table and adds them if not.
     * @param guild that should be checked.
     */
    void startUpEntries(Guild guild) throws NullPointerException;
    /**
     * Checks if there are lines for this guild in the table and adds them if not.
     * @param guildId the id from the guild that should be checked.
     */
    void startUpEntries(long guildId) throws NullPointerException;

    /**
     * Sets the mute role for the provided guild to a new value.
     * @param guild the guild.
     * @param role the new mute role.
     */
    void setMuteRole(Guild guild, Role role) throws NullPointerException;
    /**
     * Sets the mute role for the provided guild to a new value.
     * @param guild the guild.
     * @param roleId the id of the new mute role.
     */
    void setMuteRole(Guild guild, long roleId) throws NullPointerException;
    /**
     * Returns the current mute role from the provided guild.
     * @param guild the guild.
     * @return The saved mute role. Can be null.
     */
    Role getMuteRole(Guild guild);

    /**
     * Sets the special role for the provided guild to a new value.
     * @param guild the guild.
     * @param role the new specialRole.
     */
    void setSpecialRole(Guild guild, Role role) throws NullPointerException;
    /**
     * Sets the special role for the provided guild to a new value.
     * @param guild the guild.
     * @param roleId the id of the new specialRole.
     */
    void setSpecialRole(Guild guild, long roleId) throws NullPointerException;
    /**
     * Returns the current special role from the provided guild.
     * @param guild the guild.
     * @return The saved specialRole.
     */
    Role getSpecialRole(Guild guild);

    /**
     * Sets the verifiable role for the provided guild to a new value.
     * @param guild the guild.
     * @param role the new verifiableRole.
     */
    void setVerifyRole(Guild guild, Role role) throws NullPointerException;
    /**
     * Sets the verifiable role for the provided guild to a new value.
     * @param guild the guild.
     * @param roleId the id of the new verifiableRole.
     */
    void setVerifyRole(Guild guild, long roleId) throws NullPointerException;
    /**
     * Returns the current verifiable role from the provided guild.
     * @param guild the guild.
     * @return The saved specialRole.
     */
    Role getVerifyRole(Guild guild);

    /**
     * Sets the special invite code for the provided guild to a new value.
     * @param guild the guild.
     * @param code the invite-code.
     */
    void setSpecialCode(Guild guild, String code) throws NullPointerException;
    /**
     * Returns the current special invite-code from the provided guild.
     * @param guild the guild.
     * @return The saved invite-code.
     */
    String getSpecialCode(Guild guild);

    /**
     * Removes all entries where the guild was left.
     * @return true if successful, otherwise false.
     */
    boolean removeUnusedEntries();

    /**
     * Removes all entries that are connected to the provided guild.
     * @param guild The guild whose entries should be deleted.
     * @return true if successful, otherwise false.
     */
    boolean removeEntriesByGuildId(Guild guild);
    /**
     * Removes all entries that are connected to the provided guild.
     * @param guildId The id of the guild whose entries should be deleted.
     * @return true if successful, otherwise false.
     */
    boolean removeEntriesByGuildId(long guildId);
}
