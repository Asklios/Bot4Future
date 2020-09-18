package main.java.files.interfaces;

import java.sql.SQLException;
import java.util.Map;

public interface UserRecordsDatabase {

    void addRecord(long userID, long date, long endtime, String type, long guildID, String reason, String note);
    Map<String,Integer> recordNumbers(long userID) throws SQLException;

    void setBanLifted(long userID, long guildID);

    boolean unbanRequestValue(long userID, long guildID);
}
