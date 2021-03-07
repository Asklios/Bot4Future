package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.InviteDatabase;
import net.dv8tion.jda.api.entities.Member;

/**
 * Class for requests to the invitemanager table in the database.
 *
 * @author Asklios
 * @version 06.12.2020
 */
public class InviteDatabaseSQLite implements InviteDatabase {

    /**
     * Saves the specialMember and the newly verified.
     * @param specialMember the member using the verify command.
     * @param verifiedMember the member that was verified.
     */
    @Override
    public void saveVerified(Member specialMember, Member verifiedMember) {
        long specialUserId = specialMember.getIdLong();
        long verifiedUserId = verifiedMember.getIdLong();
        long guildId = specialMember.getGuild().getIdLong();

        LiteSQL.onUpdate("INSERT INTO invitemanager(guildid, specialuserid, verifieduserid) VALUES(" + guildId + ", " + specialUserId +
                ", " + verifiedUserId +")");
    }

    /**
     * Saves the new specialMember.
     * @param specialMember the member using the special invite code.
     */
    @Override
    public void saveSpecialMember(Member specialMember) {
        long specialUserId = specialMember.getIdLong();
        long guildId = specialMember.getGuild().getIdLong();

        LiteSQL.onUpdate("INSERT INTO invitemanager(guildid, specialuserid) VALUES(" + guildId + ", " + specialUserId + ")");
    }

    /**
     * Deletes all entries from the provided guild.
     * @param guildId the id of the requesting guild.
     */
    @Override
    public void deleteGuildData(long guildId) {
        LiteSQL.onUpdate("DELETE FROM invitemanager WHERE guildid = " + guildId);
    }
}
