package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.InviteDatabase;
import net.dv8tion.jda.api.entities.Member;

public class InviteDatabaseSQLite implements InviteDatabase {

    @Override
    public void saveVerified(Member specialMember, Member verifiedMember) {
        long specialUserId = specialMember.getIdLong();
        long verifiedUserId = verifiedMember.getIdLong();
        long guildId = specialMember.getGuild().getIdLong();

        LiteSQL.onUpdate("INSERT INTO invitemanager(guildid, specialuserid, verifieduserid) VALUES(" + guildId + ", " + specialUserId +
                ", " + verifiedUserId +")");
    }

    @Override
    public void saveSpecialMember(Member specialMember) {
        long specialUserId = specialMember.getIdLong();
        long guildId = specialMember.getGuild().getIdLong();

        LiteSQL.onUpdate("INSERT INTO invitemanager(guildid, specialuserid) VALUES(" + guildId + ", " + specialUserId + ")");
    }
}
