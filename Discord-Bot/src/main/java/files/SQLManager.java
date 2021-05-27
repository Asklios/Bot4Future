package main.java.files;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
     * @return true if successful
     */
    public static boolean onCreate() {

        try {
            Connection connection = LiteSQL.POOL.getConnection();
            Statement stmt = connection.createStatement();
            stmt.addBatch("CREATE TABLE IF NOT EXISTS guildroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, roleid INTEGER, code STRING, type STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS guildchannels(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, type STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS reactroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emote VARCHAR, roleid INTEGER)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS unbanhandlerreactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, bannedid INTEGER)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS votereactions(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emotes STRING, texts STRING, value STRING, users String)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS userrecords(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, userid INTEGER, date INTEGER, endtime INTEGER, type STRING, guildid INTEGER, reason STRING, note STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS invitemanager(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,guildid INTEGER, specialuserid INTEGER, verifieduserid INTEGER)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS calldata(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, userids STRING, starttime INTEGER, endtime INTEGER, name STRING, requester INTEGER, note STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS timedtasks(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, endtime INTEGER NOT NULL, type STRING NOT NULL, note STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS selfroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, role STRING, roleid INTEGER)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS polls(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, msgid INTEGER, name STRING, description STRING, votesperuser INTEGER, endtime INTEGER, ownerid INTEGER, hidevotes INTEGER)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS pollchoices(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, value STRING)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS pollvotes(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pollguildid INTEGER, pollmsgid INTEGER, choiceid INTEGER, userid INTEGER)");
            stmt.executeBatch();
            stmt.close();
            connection.close();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
