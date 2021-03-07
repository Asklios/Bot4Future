package main.java.helper;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class UserRecord {

    private long id;
    private long userId;
    private long date;
    private long endTime;
    private RecordType type;
    private long guildId;
    private String reason;
    private String note;

    public UserRecord(long id, long userId, long date, long endTime, RecordType type, long guildId, String reason, String note) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.endTime = endTime;
        this.type = type;
        this.guildId = guildId;
        this.reason = reason;
        this.note = note;
    }

    public enum RecordType {
        WARNING, MUTE, TEMPMUTE, BAN, TEMPBAN, UNBANREQUEST
    }
}
