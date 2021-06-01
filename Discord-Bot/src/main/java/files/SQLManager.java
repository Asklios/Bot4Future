package main.java.files;

/**
 * Class for creating missing tables in the database on startup.
 *
 * @author Asklios
 * @version 18.11.2020
 */

public class SQLManager {

    /**
     * Creates missing tables in database.
     * Should be executed on startup.
     */
    public static void onCreate() {

        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS guildroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, roleid INTEGER, code STRING, type STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS guildchannels(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, type STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS reactroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emote VARCHAR, roleid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS unbanhandlerreactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, bannedid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS votereactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emotes STRING, texts STRING, value STRING, users String)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS userrecords(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid INTEGER, date INTEGER, endtime INTEGER, type STRING, guildid INTEGER, reason STRING, note STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS invitemanager(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,guildid INTEGER, specialuserid INTEGER, verifieduserid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS calldata(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, userids STRING, starttime INTEGER, endtime INTEGER, name STRING, requester INTEGER, note STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS timedtasks(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, endtime INTEGER NOT NULL, type STRING NOT NULL, note STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS selfroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, role STRING, roleid INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS polls(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, msgid STRING, name STRING, description STRING, votesperuser INTEGER, endtime INTEGER, ownerid INTEGER, hidevotes INTEGER)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS pollchoices(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, value STRING)");
        LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS pollvotes(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, userid INTEGER)");

        if(System.getenv("RESETPOLLS") != null && System.getenv("RESETPOLLS").equalsIgnoreCase("true")){
            LiteSQL.onUpdate("DROP TABLE polls;");
            LiteSQL.onUpdate("DROP TABLE pollvotes;");
            LiteSQL.onUpdate("DROP TABLE pollchoices;");
            LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS polls(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, msgid STRING, name STRING, description STRING, votesperuser INTEGER, endtime INTEGER, ownerid INTEGER, hidevotes INTEGER)");
            LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS pollchoices(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, value STRING)");
            LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS pollvotes(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, userid INTEGER)");

        }
    }
}
