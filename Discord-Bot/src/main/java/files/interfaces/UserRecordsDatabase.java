package main.java.files.interfaces;

import main.java.helper.UserRecord;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface UserRecordsDatabase {

    UserRecord addRecord(long userID, long date, long endtime, String type, long guildID, String reason, String note);

    void setBanLifted(long userID, long guildID);

    void setNoteLiftedById(long id);

    void setNoteById(long id, String note);

    void removeGuildData(long guildId);

    Map<String, Integer> recordNumbers(long userId);

    boolean isUnbanRequest(long guildId, long userId);

    boolean wasMuted(long guildId, long userId);

    List<Long> unliftedMutesByUserIdAndGuildId(long userId, long guildId);

    String recordNoteById(long id);

    UserRecord recordById(long id);
}
