package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.UnbanHandlerDatabase;

public class UnbanHandlerDatabaseSQLite implements UnbanHandlerDatabase {

    @Override
    public void addVoteReactions(long guildId, long auditChannelId, long auditMessageId, long bannedId) {
        LiteSQL.onUpdate("INSERT INTO unbanhandlerreactions(guildid, channelid, messageid, bannedid) VALUES(" +
                guildId + ", " + auditChannelId + ", " + auditMessageId + ", " + bannedId + ")");
    }

    @Override
    public void removeGuildData(long guildId) {
        LiteSQL.onUpdate("DELETE FROM unbanhandlerreactions WHERE guildid = " + guildId);
    }
}
