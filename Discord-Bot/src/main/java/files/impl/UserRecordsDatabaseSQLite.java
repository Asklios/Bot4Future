package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.UserRecord;
import main.java.helper.UserRecords;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRecordsDatabaseSQLite implements UserRecordsDatabase {

    @Override
    public UserRecord addRecord(long userId, long date, long endTime, String type, long guildId, String reason, String note) {
        if (endTime != 0) {
            PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO userrecords(userid, date, endtime, type, guildid, reason, note) VALUES(?,?,?,?,?,?,?)");
            try {
                prepStmt.setLong(1, userId);
                prepStmt.setLong(2, date);
                prepStmt.setLong(3, endTime);
                prepStmt.setString(4, type);
                prepStmt.setLong(5, guildId);
                prepStmt.setString(6, reason);
                prepStmt.setString(7, note);
                prepStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO userrecords(userid, date, type, guildid, reason, note) VALUES(?,?,?,?,?,?)");
            try {
                prepStmt.setLong(1, userId);
                prepStmt.setLong(2, date);
                prepStmt.setString(3, type);
                prepStmt.setLong(4, guildId);
                prepStmt.setString(5, reason);
                prepStmt.setString(6, note);
                prepStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT id FROM userrecords WHERE userid = ? AND date = ? AND type = ? AND guildid = ? AND reason = ? AND note = ?");
        ResultSet result = null;
        try {
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2, date);
            prepStmt.setString(3, type);
            prepStmt.setLong(4, guildId);
            prepStmt.setString(5, reason);
            prepStmt.setString(6, note);
            result = prepStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long id = 0;
        try {
            assert result != null;
            id = result.getLong("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (id != 0) return new UserRecords().addUserRecord(id, userId, date, endTime, type, guildId, reason, note);
        return null;
    }

    @Override
    public void setBanLifted(long userID, long guildID) {

        LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE guildID = " + guildID + " AND userid = " + userID + " AND type = 'ban'");
        LiteSQL.onUpdate("DELETE FROM userrecords WHERE guildID = " + guildID + " AND userid = " + userID + " AND type = 'unbanrequest'");
        try {
            new UserRecords().setNoteByGuildUserType(guildID, userID, UserRecord.RecordType.BAN, "lifted");
        } catch (IndexOutOfBoundsException e) {
            //
        }
        try {
            new UserRecords().setNoteByGuildUserType(guildID, userID, UserRecord.RecordType.TEMPBAN, "lifted");
        } catch (IndexOutOfBoundsException f) {
            //
        }
    }

    @Override
    public void setNoteLiftedById(long id) {
        LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE Id = " + id);
    }

    @Override
    public void setNoteById(long id, String note) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("UPDATE userrecords SET note = ? WHERE Id = ?");
        try {
            assert prepStmt != null;
            prepStmt.setString(1, note);
            prepStmt.setLong(2, id);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeGuildData(long guildId) {
        LiteSQL.onUpdate("DELETE FROM userrecords WHERE guildid = " + guildId);
        new UserRecords().removeUserRecordsByGuildId(guildId);
    }

    @Override
    public ResultSet AllUserRecords() {
        return LiteSQL.onQuery("SELECT * FROM userrecords");
    }
}
