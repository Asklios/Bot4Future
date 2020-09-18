package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.UserRecordsDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserRecordsDatabaseSQLite implements UserRecordsDatabase {

    @Override
    public void addRecord(long userID, long date, long endtime, String type, long guildID, String reason, String note) {
        if (endtime != 0) {
            LiteSQL.onUpdate("INSERT INTO userrecords(userid, date, endtime, type, guildid, reason, note) VALUES(" +
                    userID + ", " + date + ", " + endtime + ", '" + type + "', " + guildID + ", '" + reason + "', '" + note + "')");
        }
        else {
            LiteSQL.onUpdate("INSERT INTO userrecords(userid, date, type, guildid, reason, note) VALUES(" +
                    userID + ", " + date + ", '" + type + "', " + guildID + ", '" + reason + "', '" + note + "')");
        }
    }

    @Override
    public Map recordNumbers(long userID) {
        ResultSet results = LiteSQL.onQuery("SELECT type, note FROM userrecords WHERE userid = " + userID);
        Map<String,Integer> records = new HashMap<>();

        if (results == null) return null;

        records.put("ban", 0);
        records.put("warning", 0);
        records.put("mute", 0);
        records.put("bantotal", 0);
        records.put("warntotal", 0);
        records.put("mutetotal", 0);

        try {
            while (results.next()) {
                String type = results.getString("type");
                String note = results.getString("note");
                if (!note.equals("lifted")) {
                    int i = records.get(type);
                    i++;
                    records.replace(type, i);

                    if (type.equals("ban")) {
                        int j = records.get("bantotal");
                        j++;
                        records.replace("bantotal", j);
                    }
                    else if (type.equals("tempban")) {
                        int j = records.get("bantotal");
                        j++;
                        records.replace("bantotal", j);
                    }
                    else if (type.equals("warning")) {
                        int j = records.get("warntotal");
                        j++;
                        records.replace("warntotal", j);
                    }
                    else if (type.equals("mute")) {
                        int j = records.get("mutetotal");
                        j++;
                        records.replace("mutetotal", j);
                    }
                }
                else if (type.equals("ban")) {
                    int j = records.get("bantotal");
                    j++;
                    records.replace("bantotal", j);
                }
                else if (type.equals("tempban")) {
                    int j = records.get("bantotal");
                    j++;
                    records.replace("bantotal", j);
                }
                else if (type.equals("warning")) {
                    int j = records.get("warntotal");
                    j++;
                    records.replace("warntotal", j);
                }
                else if (type.equals("mute")) {
                    int j = records.get("mutetotal");
                    j++;
                    records.replace("mutetotal", j);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public void setBanLifted(long userID, long guildID) {

        LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE guildID = " + guildID + " AND userid = " + userID);
        LiteSQL.onUpdate("DELETE FROM userrecords WHERE guildID = " + guildID + " AND userid = " + userID + " AND type = 'unbanrequest'");

    }

    @Override
    public boolean unbanRequestValue(long userID, long guildID) {
        ResultSet results = LiteSQL.onQuery("SELECT * FROM userrecords WHERE userid = " + userID + " AND guildid = " + guildID + " AND type = 'unbanrequest'");

        try {
            if (results.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
