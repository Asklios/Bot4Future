package main.java.files.interfaces;

import main.java.helper.UserRecord;

import java.sql.ResultSet;

public interface UserRecordsDatabase {

    UserRecord addRecord(long userID, long date, long endtime, String type, long guildID, String reason, String note);

    void setBanLifted(long userID, long guildID);

    void setNoteLiftedById(long id);

    void setNoteById(long id, String note);

    void removeGuildData(long guildId);

    ResultSet AllUserRecords();
}
