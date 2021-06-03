package main.java.helper.api;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class Strikes {

    @Getter
    private static final List<Strike> strikes = new ArrayList<>();

    /**
     * Deletes all saved Strikes.
     */
    public void deleteAll() {
        strikes.clear();
    }

    /**
     * Adds a new Strike to the strikes HashMap.
     */
    public void addStrike(long id, @NonNull String locationName, @NonNull String localGroupName,
                          double lat, double lon, String state, @NonNull Date dateTime, String note,
                          String eventLink, Date lastUpdate) {
        Strike strike = new Strike(id, locationName, localGroupName, lat, lon, state, dateTime, note, eventLink, lastUpdate);
        strikes.add(strike);
    }

    /**
     * Checks if there is a strike with the provided id.
     *
     * @return true if there is one, otherwise false.
     */
    public boolean containsId(long id) {

        return strikes
                .stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .isPresent();
    }

    /**
     * Finds all strikes for the specified location.
     *
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByLocationName(String locationName) {
        return strikes
                .stream()
                .filter(strike -> strike.getLocationName().equalsIgnoreCase(locationName))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Finds all strikes for the specified group.
     *
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByLocalGroupName(String locationGroupName) {
        return strikes
                .stream()
                .filter(strike -> strike.getLocalGroupName().equalsIgnoreCase(locationGroupName))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Finds all strikes on the specified date.
     *
     * @return List of Strike. May be empty.
     */
    public List<Strike> getStrikesByDate(Date date) {
        return strikes
                .stream()
                .filter(strike -> strike.getDateTime().compareTo(date) == 0)
                .collect(Collectors.toUnmodifiableList());
    }
}