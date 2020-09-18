package main.java.files;

public class SQLManager {

    public static void onCreate() {

        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS guildroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, roleid INTEGER, code STRING, type STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS guildchannels(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, type STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS reactroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emote VARCHAR, roleid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS unbanhandlerreactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, bannedid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS votereactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emotes STRING, texts STRING, value STRING, users String)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS userrecords(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid INTEGER, date INTEGER, endtime INTEGER, type STRING, guildid INTEGER, reason STRING, note STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS invitemanager(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,guildid INTEGER, specialuserid INTEGER, verifieduserid INTEGER)");

    }
}
