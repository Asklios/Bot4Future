package main.java.helper.api;

import lombok.Getter;
import lombok.NonNull;

import java.util.*;

public class LocalGroups {

    @Getter
    private static HashMap<Long, LocalGroup> localGroups = new HashMap<>();

    //index HashMaps
    private static HashMap<String, List<Long>> localGroupsByName = new HashMap<>();
    private static HashMap<String, List<Long>> localGroupsByState = new HashMap<>();

    /**
     * Deletes all saved LocalGroups.
     */
    public void deleteAll() {
        localGroups.clear();
        localGroupsByName.clear();
        localGroupsByState.clear();
    }

    public void addLocalGroup(@NonNull long id, @NonNull String name, double lat, double lon, @NonNull String state, String facebook,
                              String instagram, String twitter, String youtube, String website, String whatsapp, String telegram,
                              String other, String email) {
        LocalGroup localGroup = new LocalGroup(id,name, lat, lon, state, facebook, instagram, twitter, youtube, website, whatsapp,
                telegram, other, email);
        localGroups.put(id, localGroup);
        addIndex(id, name, state);
    }

    private void addIndex(long id, String name, String state) {

        if (localGroupsByName.containsKey(name.toLowerCase())) {
            List<Long> ids = localGroupsByName.get(name.toLowerCase());
            ids.add(id);
            localGroupsByName.replace(name.toLowerCase(), ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            localGroupsByName.put(name.toLowerCase(), ids);
        }

        if (localGroupsByState.containsKey(state.toLowerCase())) {
            List<Long> ids = localGroupsByState.get(state.toLowerCase());
            ids.add(id);
            localGroupsByState.replace(state.toLowerCase(), ids);
        }
        else {
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            localGroupsByState.put(state.toLowerCase(), ids);
        }
    }

    /**
     * Checks if there is a LocalGroup with the provided id.
     * @return true if there is one, otherwise false.
     */
    public boolean containsId(long id) {
        return localGroups.containsKey(id);
    }

    /**
     * Finds the group with the specified name.
     * @return LocalGroup or null.
     */
    public LocalGroup getGroupByName(String name) {
        return LocalGroups.localGroups.values()
                .stream()
                .filter(localGroup -> localGroup.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds the group with the specified id
     * @param id
     * @return LocalGroup or null
     */
    public LocalGroup getGroupById(Long id) {
        return localGroups.values()
                .stream()
                .filter(localGroup -> localGroup.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds all groups in the specified state.
     * @return List of LocalGroup. May be empty.
     */
    public List<LocalGroup> getLocalGroupsByState(String state) {
        List<Long> ids = localGroupsByState.get(state);
        List<LocalGroup> localGroups = new ArrayList<>();
        ids.forEach(id -> localGroups.add(LocalGroups.localGroups.get(id)));
        return localGroups;
    }

    public LocalGroup[] getLocalGroups() {
        LocalGroup[] groups = LocalGroups.localGroups.values().toArray(LocalGroup[]::new);
        Arrays.sort(groups);
        return (groups);
    }
}
