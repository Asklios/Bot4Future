package main.java.helper;

import main.java.files.LiteSqlClear;
import main.java.files.impl.TimedTasksDatabaseSQLite;
import main.java.files.interfaces.TimedTasksDatabase;
import main.java.helper.api.UpdateFromApi;

import java.util.*;

/**
 * Class for scheduling Tasks for the future.
 *
 * @author Asklios
 * @version 31.12.2020
 */
public class TimedTasks {

    private final TimedTasksDatabase timedTasksDatabase = new TimedTasksDatabaseSQLite();

    /**
     * HashMap of all scheduled tasks.
     */
    public static HashMap<Long, TimedTask> tasks = new HashMap<>();

    /**
     * Starts the scheduler Thread. Should be executed on startup.
     */
    public static void startTimedTasks() {
        new TimedTasksDatabaseSQLite().updateAllTasksFromDb();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new TimedTasks().scheduler();
    }

    /**
     * The scheduler thread. Will be executed every 15 seconds.
     */
    private void scheduler() {

        Thread thread = new Thread(() -> {

            while (true) {
                ArrayList<Long> nextTasks = new ArrayList<>();
                try {
                    tasks.keySet().stream().sorted(Comparator.naturalOrder()).forEach(nextTasks::add);
                } catch (NoSuchElementException e) {
                    //
                }

                if (!nextTasks.isEmpty()) {
                    taskFinder(nextTasks);
                }

                try {
                    Thread.sleep(15 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * If there are scheduled tasks this method will point to the corresponding class.
     */
    private void taskFinder(ArrayList<Long> nextTask) {
        for (long l : nextTask) {
            if (l > System.currentTimeMillis()) {
                return;
            }
            TimedTask.TimedTaskType task = tasks.get(l).getType();
            String taskName = task.toString();

            switch (taskName){
                case "COUNTDOWN": PbCountdownWriter.countdownUpdater(); removeTimedTask(tasks.get(l)); break;
                case "UNMUTE": new UnMute().liftMute(tasks.get(l)); removeTimedTask(tasks.get(l)); break;
                case "UNBAN": new UnBan().liftBan(tasks.get(l)); removeTimedTask(tasks.get(l)); break;
                case "APIUPDATE": new UpdateFromApi().completeUpdate(); removeTimedTask(tasks.get(l)); break;
                case "DBCLEAR": new LiteSqlClear().clearDatabase(); removeTimedTask(tasks.get(l)); break;
            }
        }
    }

    /**
     * Converts a String to the corresponding TimedTaskType
     * @param typeString {COUNTDOWN, UNMUTE, UNBAN, UPDATEAPI, DBCLEAR}
     * @return TimedTaskType or null
     */
    public TimedTask.TimedTaskType getTimedTaskTypeFromString(String typeString) {
        switch (typeString) {
            case "COUNTDOWN": return TimedTask.TimedTaskType.COUNTDOWN;
            case "UNMUTE": return TimedTask.TimedTaskType.UNMUTE;
            case "UNBAN":  return TimedTask.TimedTaskType.UNBAN;
            case "APIUPDATE":  return TimedTask.TimedTaskType.APIUPDATE;
            case "DBCLEAR":  return TimedTask.TimedTaskType.DBCLEAR;
        }
        return null;
    }

    /**
     * Deletes all entries from the tasks HashMap.
     */
    public void clearTimedTasks() {
        tasks.clear();
    }

    /**
     * Adds a new TimedTask to the database and the tasks HashMap.
     * @param timedTaskType of TimedTask.
     * @param endTime Time of execution in milliseconds.
     */
    public void addTimedTask(TimedTask.TimedTaskType timedTaskType, long endTime) {
        tasks.put(endTime, new TimedTask(timedTaskType, endTime));
        timedTasksDatabase.addTask(endTime, timedTaskType.toString());
    }

    /**
     * Adds a new TimedTask to the database and the tasks HashMap.
     * @param timedTaskType of TimedTask.
     * @param endTime Time of execution in milliseconds.
     * @param note optional extra information.
     */
    public void addTimedTask(TimedTask.TimedTaskType timedTaskType, long endTime, String note) {
        tasks.put(endTime, new TimedTask(timedTaskType, endTime, note));
        timedTasksDatabase.addTask(endTime, timedTaskType.toString(), note);
    }

    /**
     * Adds a new TimedTask to the tasks HashMap.
     * @param timedTaskType of TimedTask.
     * @param endTime Time of execution in milliseconds.
     */
    public void addTimedTasktoHashMap(TimedTask.TimedTaskType timedTaskType, long endTime) {
        tasks.put(endTime, new TimedTask(timedTaskType, endTime));
    }

    /**
     * Adds a new TimedTask to the tasks HashMap.
     * @param timedTaskType of TimedTask.
     * @param endTime Time of execution in milliseconds.
     * @param note optional extra information.
     */
    public void addTimedTasktoHashMap(TimedTask.TimedTaskType timedTaskType, long endTime, String note) {
        tasks.put(endTime, new TimedTask(timedTaskType, endTime, note));
    }

    /**
     * Removes a TimedTask from the tasks HashMap.
     * @param timedTask that should be removed.
     * @return true if successful, otherwise false.
     */
    public boolean removeTimedTask(TimedTask timedTask) {
        long endTime = timedTask.getEndTime();
        timedTasksDatabase.removeTask(timedTask.getEndTime(), timedTask.getType().toString());
        return tasks.remove(endTime, timedTask);
    }

    /**
     * Returns a list of all TimedTasks of the specified type.
     * @return List of TimedTasks.
     */
    public List<TimedTask> timedTasksByType(TimedTask.TimedTaskType taskType) {
        List<TimedTask> timedTasks = new ArrayList<>();
        tasks.forEach((l, t) -> {
            if (t.getType() == taskType) {
                timedTasks.add(t);
            }
        });
        return timedTasks;
    }

    /**
     * Removes TimedTasks where the UserRecord is missing (Type: UNBAN, UNMUTE)
     */
    public void removeOldTasks() {
        timedTasksByType(TimedTask.TimedTaskType.UNBAN).forEach(t -> {
            UserRecord userRecord = new UserRecords().userRecordById(Long.parseLong(t.getNote()));
            if (userRecord == null) removeTimedTask(t);
            else if (userRecord.getNote().equals("lifted")) removeTimedTask(t);
        });
        timedTasksByType(TimedTask.TimedTaskType.UNMUTE).forEach(t -> {
            UserRecord userRecord = new UserRecords().userRecordById(Long.parseLong(t.getNote()));
            if (userRecord == null) removeTimedTask(t);
            else if (userRecord.getNote().equals("lifted")) removeTimedTask(t);
        });
    }
}
