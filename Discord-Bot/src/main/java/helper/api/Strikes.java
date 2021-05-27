package main.java.helper.api;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Strikes {

    @Getter
    private static final HashMap<Long, Strike> strikes = new HashMap<>();

    //index HashMaps
    private static final HashMap<String, List<Long>> strikesByLocationName = new HashMap<>();
    private static final HashMap<String, List<Long>> strikesByLocalGroupName = new HashMap<>();
    private static final HashMap<Date, List<Long>> strikesByDate = new HashMap<>();

    /**
     * Deletes all saved Strikes.
     */
    public void deleteAll() {
        strikes.clear();
        strikesByLocationName.clear();
        strikesByLocalGroupName.clear();
        strikesByDate.clear();
    }

    /**
     * Adds a new Strike to the strikes HashMap.
     */
    public void addStrike(@NonNull long id, @NonNull String locationName, @NonNull String localGroupName,
                          double lat, double lon, String state, @NonNull Date dateTime, String note,
                          String eventLink, Date lastUpdate){
        Strike strike = new Strike(id,locationName, localGroupName, lat, lon, state, dateTime, note, eventLink, lastUpdate);
        strikes.put(id, strike);
        addIndex(id, locationName, localGroupName, dateTime);
    }

    /**
     * Adds a new link to the index maps.
     * @param id of the entry.
     */
    private void addIndex(long id, String locationName, String localGroupName, Date dateTime) {

        if (strikesByLocationName.containsKey(locationName)) {
            List<Long> ids = strikesByLocationName.get(locationName);
            ids.add(id);
            strikesByLocationName.replace(locationName, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            strikesByLocationName.put(locationName, ids);
        }

        if (strikesByLocalGroupName.containsKey(localGroupName)) {
            List<Long> ids = strikesByLocalGroupName.get(localGroupName);
            ids.add(id);
            strikesByLocalGroupName.replace(localGroupName, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            strikesByLocalGroupName.put(localGroupName, ids);
        }

        if (strikesByDate.containsKey(dateTime)) {
            List<Long> ids = strikesByDate.get(dateTime);
            ids.add(id);
            strikesByDate.replace(dateTime, ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            strikesByDate.put(dateTime, ids);
        }
    }

    /**
     * Checks if there is a strike with the provided id.
     * @return true if there is one, otherwise false.
     */
    public boolean containsId(long id) {
        return strikes.containsKey(id);
    }

    /**
     * Finds all strikes for the specified location.
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByLocationName(String locationName) {
        List<Long> ids = strikesByLocationName.get(locationName);
        List<Strike> strikes = new ArrayList<>();
        ids.forEach(id -> strikes.add(Strikes.strikes.get(id)));
        return strikes;
    }

    /**
     * Finds all strikes for the specified group.
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByLocalGroupName(String locationGroupName) {
        List<Long> ids = strikesByLocalGroupName.get(locationGroupName);
        List<Strike> strikes = new ArrayList<>();
        ids.forEach(id -> strikes.add(Strikes.strikes.get(id)));
        return strikes;
    }

    /**
     * Finds all strikes on the specified date.
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByDate(Date date) {
        List<Long> ids = strikesByDate.get(date);
        List<Strike> strikes = new ArrayList<>();
        ids.forEach(id -> strikes.add(Strikes.strikes.get(id)));
        return strikes;
    }
}