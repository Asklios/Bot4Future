package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.helper.UserRecord;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRecordsDatabaseSQLite implements UserRecordsDatabase {

    @Override
    public UserRecord addRecord(long userId, long date, long endTime, String type, long guildId, String reason, String note) {
        if (endTime != 0) {
            PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO userrecords(userid, date, endtime, type, guildid, reason, note) VALUES(?,?,?,?,?,?,?)");
            try {
                assert prepStmt != null;
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
                assert prepStmt != null;
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
            assert prepStmt != null;
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
            LiteSQL.closePreparedStatement(prepStmt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (id != 0) return new UserRecord (id, userId, date, endTime, recordTypeFromTypeString(type), guildId, reason, note);
        return null;
    }

    @Override
    public void setBanLifted(long userID, long guildID) {
        LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE guildID = " + guildID + " AND userid = " + userID + " AND type = 'ban'");
        LiteSQL.onUpdate("DELETE FROM userrecords WHERE guildID = " + guildID + " AND userid = " + userID + " AND type = 'unbanrequest'");
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
    }

    @Override
    public ResultSet AllUserRecords() {
        return LiteSQL.onQuery("SELECT * FROM userrecords");
    }


    /**
     * Creates a list which contains the number of entries of a user.
     * @param userId of the user to look for.
     * @return List with the following keys (ban, warning, mute, bantotal, mutetotal)
     */
    @Override
    public Map<String, Integer> recordNumbers(long userId) {

        Map<String, Integer> records = new HashMap<>();

        records.put("ban", 0);
        records.put("warning", 0);
        records.put("mute", 0);
        records.put("bantotal", 0);
        records.put("mutetotal", 0);

        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM userrecords WHERE userid = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, userId);
            ResultSet result = prepStmt.executeQuery();
            while(result.next()) {
                String type = result.getString("type");
                String note = result.getString("note");

                switch (type) {
                    case "warning": {
                        if (!note.equals("lifted")) records.put("warning", records.get("warning") + 1);
                        break;
                    }
                    case "mute": {
                        if (!note.equals("lifted")) records.put("mute", records.get("mute") + 1);
                        records.put("mutetotal", records.get("mutetotal") + 1);
                        break;
                    }
                    case "tempban":
                    case "ban": {
                        if (!note.equals("lifted")) records.put("ban", records.get("ban") + 1);
                        records.put("bantotal", records.get("bantotal") + 1);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Checks if an UnbanRequest exists for the user.
     * @param guildId the record is from.
     * @param userId to which the record belongs.
     * @return true if there is an unbanRequest, otherwise false.
     */
    @Override
    public boolean isUnbanRequest(long guildId, long userId) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM userrecords WHERE userid = ? AND guildid = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2,guildId);
            ResultSet result = prepStmt.executeQuery();

            if (result.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Search for a mute which is not lifted
     * @return true if found otherwise false
     */
    @Override
    public boolean wasMuted(long guildId, long userId) {
        ResultSet result = null;
        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM userrecords WHERE userid = ? AND guildid = ? AND type = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2, guildId);
            result = prepStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null) return false;

        try {
            if (result.next()) {
                String note = result.getString("note");
                if (!note.equals("lifted")) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds all active mutes
     * @return A list of record ids
     */
    @Override
    public List<Long> unliftedMutesByUserIdAndGuildId(long userId, long guildId) {

        List<Long> ids = new ArrayList<>();

        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM userrecords WHERE userid = ? AND guildid = ? AND note = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2, guildId);
            prepStmt.setString(3, "lifted");
            ResultSet result = prepStmt.executeQuery();

            while(result.next()) {
                ids.add(result.getLong("id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * @return the note of the requested entry, if none is found null
     */
    @Override
    public String recordNoteById(long id) {
        ResultSet result = LiteSQL.onQuery("SELECT FROM userrecords WHERE id =" + id);
        try {
            assert result != null;
            if (result.next()) {
                return result.getString("note");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates an UserRecord of the provided id
     * @return UserRecord
     */
    @Override
    public UserRecord recordById(long id) {

        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM userrecords WHERE id = ?");

        try {
            assert prepStmt != null;
            prepStmt.setLong(1, id);
            ResultSet result = prepStmt.executeQuery();

            if (!result.next()) return null;

            long userId = result.getLong("userid");
            long date = result.getLong("date");
            long endTime = result.getLong("endtime");
            UserRecord.RecordType recordType = recordTypeFromTypeString(result.getString("type"));
            long guildId = result.getLong("guildid");
            String reason = result.getString("reason");
            String note = result.getString("note");

            return new UserRecord(id, userId, date, endTime, recordType, guildId, reason, note);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserRecord.RecordType recordTypeFromTypeString(String type) {
        switch (type) {
            //WARNING, MUTE, TEMPMUTE, BAN, TEMPBAN, UNBANREQUEST
            case ("warning"): return UserRecord.RecordType.WARNING;
            case ("mute"): return UserRecord.RecordType.MUTE;
            case ("tempmute"): return UserRecord.RecordType.TEMPMUTE;
            case ("ban"): return UserRecord.RecordType.BAN;
            case ("tempban"): return UserRecord.RecordType.TEMPBAN;
            case ("unbanrequest"): return UserRecord.RecordType.UNBANREQUEST;
        }
        System.err.println("Missing RecordType (" + this.getClass().getName() +
                " line: " + Thread.currentThread().getStackTrace()[1].getLineNumber() + ")");
        return null;
    }

    private String typeStringFromRecordType(UserRecord.RecordType recordType) {
        switch (recordType) {
            case WARNING: return "warning";
            case MUTE: return "mute";
            case TEMPMUTE: return "tempmute";
            case BAN: return "ban";
            case TEMPBAN: return "tempban";
            case UNBANREQUEST: return "unbanrequest";
        }
        System.err.println("Missing RecordType (" + this.getClass().getName() +
                " line: " + Thread.currentThread().getStackTrace()[1].getLineNumber() + ")");
        return null;
    }
}
