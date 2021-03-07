package main.java.helper;

import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.UserRecordsDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRecords {

    public static HashMap<Long, UserRecord> userRecords = new HashMap<>();

    //index HashMaps
    private static HashMap<Long, List<Long>> entryIdsByGuildId = new HashMap<>();
    private static HashMap<UserRecord.RecordType, List<Long>> entryIdsByType = new HashMap<>();
    private static HashMap<Long, List<Long>> entryIdsByUserId = new HashMap<>();
    private static HashMap<String, List<Long>> entryIdsByNote = new HashMap<>();

    UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    /**
     * Loads the current state of the database into the userRecords HashMap.
     */
    public void updateUserRecordsFromDatabase() {
        ResultSet results = userRecordsDatabase.AllUserRecords();

        try {
            while (results.next()) {
                //id INTEGER, userid INTEGER, date INTEGER, endtime INTEGER, type STRING, guildid INTEGER, reason STRING, note STRING
                long id = results.getLong("id");
                long userId = results.getLong("userid");
                long date = results.getLong("date");
                long endTime = results.getLong("endtime");
                String typeString = results.getString("type");
                long guildId = results.getLong("guildid");
                String reason = results.getString("reason");
                String note = results.getString("note");

                UserRecord.RecordType type = recordTypeFromTypeString(typeString);

                UserRecord userRecord = new UserRecord(id, userId, date, endTime, type, guildId, reason, note);
                userRecords.put(id, userRecord);

                addSearchIndex(id, type, guildId, userId, note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds one UserRecord to the userRecords HashMap.
     */
    public UserRecord addUserRecord(long id, long userId, long date, long endTime, String typeString, long guildId, String reason, String note) {
        UserRecord.RecordType type = recordTypeFromTypeString(typeString);
        UserRecord userRecord = new UserRecord(id, userId, date, endTime, type, guildId, reason, note);
        userRecords.put(id, userRecord);
        addSearchIndex(id, type, guildId, userId, note);
        return userRecord;
    }

    /**
     * Removes all UserRecords related to the guild.
     * @param guildId of the requesting guild.
     */
    public void removeUserRecordsByGuildId(long guildId) {
        List<Long> idsGuildEntries = entryIdsByGuildId.get(guildId);
        idsGuildEntries.forEach(l -> {
            userRecords.remove(l);
            entryIdsByType.values().remove(l);
            entryIdsByUserId.values().remove(l);
            entryIdsByNote.values().remove(l);
        });
        entryIdsByGuildId.remove(guildId);
    }

    /**
     * Adds a new link to the index maps.
     * @param id Entry id from the database.
     * @param type of the entry.
     * @param guildId where the record is from.
     * @param userId the record is linked to.
     * @param note which adds extra information or indicates the status of the entry.
     */
    private void addSearchIndex(long id, UserRecord.RecordType type, long guildId, long userId, String note) {
        if (entryIdsByGuildId.containsKey(guildId)) {
            List<Long> ids = entryIdsByGuildId.get(guildId);
            ids.add(id);
            entryIdsByGuildId.replace(guildId, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            entryIdsByGuildId.put(guildId, ids);
        }

        if (entryIdsByType.containsKey(type)) {
            List<Long> ids = entryIdsByType.get(type);
            ids.add(id);
            entryIdsByType.replace(type, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            entryIdsByType.put(type, ids);
        }

        if (entryIdsByUserId.containsKey(userId)) {
            List<Long> ids = entryIdsByUserId.get(userId);
            ids.add(id);
            entryIdsByUserId.replace(userId, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            entryIdsByUserId.put(userId, ids);
        }

        if (entryIdsByNote.containsKey(note)) {
            List<Long> ids = entryIdsByNote.get(note);
            ids.add(id);
            entryIdsByNote.replace(note, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            entryIdsByNote.put(note, ids);
        }
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

    /**
     * Removes the links with the specified id.
     * @param id that should be removed.
     */
    private void removeSearchIndex(long id) {
        entryIdsByGuildId.values().remove(id);
        entryIdsByType.values().remove(id);
        entryIdsByUserId.values().remove(id);
        entryIdsByNote.values().remove(id);
    }

    /**
     * Finds all UserRecords that match the search values.
     * @param guildId as first search term.
     * @param type as second search term.
     * @return A List of all UserRecords of the specified guild with the specified type. Can be null.
     */
    public List<UserRecord> userRecordByTypeGuild(long guildId, UserRecord.RecordType type) {
        List<Long> idsByGuild = entryIdsByGuildId.get(guildId);
        List<Long> idsByType = entryIdsByType.get(type);

        if (idsByGuild.isEmpty() || idsByType.isEmpty()) {
            return null;
        }

        List<Long> matchingIds = new ArrayList<>();

        idsByGuild.forEach(l -> {
            if (idsByType.contains(l)) matchingIds.add(l);
        });

        List<UserRecord> specifiedUserRecords = new ArrayList<>();

        matchingIds.forEach(l -> specifiedUserRecords.add(userRecords.get(l)));
        return specifiedUserRecords;
    }

    /**
     * Finds the UserRecord at a known position.
     * @param id to look for.
     * @return The UserRecord that is mapped to the provided id. Can be null.
     */
    public UserRecord userRecordById(long id) {
        return userRecords.get(id);
    }

    /**
     * Finds all UserRecords that match the search values.
     * @param guildId as first search term.
     * @param type as second search term.
     * @param userId as third search term.
     * @return A List of all UserRecords of the specified guild with the specified type. Can be null.
     */
    public List<UserRecord> userRecordByTypeGuildUser(long guildId, UserRecord.RecordType type, long userId) {
        List<Long> idsByGuild = entryIdsByGuildId.get(guildId);
        List<Long> idsByType = entryIdsByType.get(type);
        List<Long> idsByUser = entryIdsByUserId.get(userId);

        if (idsByGuild.isEmpty() || idsByUser.isEmpty() || idsByType.isEmpty()) {
            return null;
        }

        List<Long> matchingIds = new ArrayList<>();

        idsByGuild.forEach(l -> {
            if (idsByType.contains(l)) {
                if (idsByUser.contains(l)) matchingIds.add(l);
            }
        });

        List<UserRecord> specifiedUserRecords = new ArrayList<>();

        matchingIds.forEach(l -> specifiedUserRecords.add(userRecords.get(l)));
        return specifiedUserRecords;
    }

    /**
     * Updates the note in the UserRecord to the new value.
     * @param guildId the record is from.
     * @param userId to which the record belongs.
     * @param type the RecordType of UserRecord.
     * @param note the new value of note.
     */
    public void setNoteByGuildUserType(long guildId, long userId, UserRecord.RecordType type, String note) {
        try {
            userRecordByTypeGuildUser(guildId, type, userId).get(0).setNote(note);
        } catch (IndexOutOfBoundsException e) {
            //
        }
    }

    /**
     * Updates the note in the UserRecord + database to the new value.
     * @param note the new value of note.
     */
    public void setNoteById(long id, String note) {
        userRecordById(id).setNote(note);
        userRecordsDatabase.setNoteById(id, note);
    }

    /**
     * Updates the note in the UserRecord + database to lifted.
     */
    public void setNoteLiftedById(long id) {
        userRecordById(id).setNote("lifted");
        userRecordsDatabase.setNoteLiftedById(id);
    }

    /**
     * Checks if an UnbanRequest exists for the user.
     * @param guildId the record is from.
     * @param userId to which the record belongs.
     * @return true if there is an unbanRequest, otherwise false.
     */
    public boolean isUnbanRequest(long guildId, long userId) {
        UserRecord.RecordType type = UserRecord.RecordType.UNBANREQUEST;
        return !userRecordByTypeGuildUser(guildId, type, userId).isEmpty();
    }

    /**
     * Creates a list which contains the number of entries of a user.
     * @param userId of the user to look for.
     * @return List with the following keys (ban, warning, mute, bantotal, mutetotal)
     */
    public Map<String, Integer> recordNumbers(long userId) {
        List<Long> entryIds = entryIdsByUserId.get(userId);
        List<UserRecord> specifiedUserRecords = new ArrayList<>();
        if (entryIds != null) entryIds.forEach(l -> specifiedUserRecords.add(userRecords.get(l)));

        Map<String, Integer> records = new HashMap<>();

        records.put("ban", 0);
        records.put("warning", 0);
        records.put("mute", 0);
        records.put("bantotal", 0);
        records.put("mutetotal", 0);

        specifiedUserRecords.forEach(u -> {
            String note = u.getNote();
            String type = typeStringFromRecordType(u.getType());

            assert type != null;
            if (type.equals("ban") || type.equals("mute")) {
                if (note.equals("liftet")) {
                    type = type + "total";
                }
            }

            int i = records.get(type);
            i++;
            records.replace(type, i);
        });

        String[] totalValues = {"bantotal", "mutetotal"};
        for (String totalValue : totalValues) {
            int i = records.get(totalValue);
            int j = records.get(totalValue.replaceAll("total", ""));
            records.replace(totalValue, i+j);
        }

        return records;
    }
}