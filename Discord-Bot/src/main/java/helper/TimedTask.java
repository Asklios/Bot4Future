package main.java.helper;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class TimedTask {

    private TimedTaskType type;
    private long endTime;
    private String note;

    /**
     * @param type of the entry.
     * @param endTime in milliseconds.
     * @param note with optional extra information.
     */
    public TimedTask(@NonNull TimedTaskType type,@NonNull long endTime, String note) {
        this.type = type;
        this.endTime = endTime;
        this.note = note;
    }

    /**
     * @param type of the entry.
     * @param endTime in milliseconds.
     */
    public TimedTask(@NonNull TimedTaskType type,@NonNull long endTime) {
        this.type = type;
        this.endTime = endTime;
    }

    public enum TimedTaskType {
        COUNTDOWN, UNMUTE, UNBAN, APIUPDATE, DBCLEAR
    }
}
