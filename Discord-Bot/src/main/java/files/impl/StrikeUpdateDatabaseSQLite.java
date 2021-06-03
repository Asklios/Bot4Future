package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.StrikeUpdateDatabase;
import main.java.helper.api.Strike;
import main.java.helper.api.Strikes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StrikeUpdateDatabaseSQLite implements StrikeUpdateDatabase {
    private final Map<Long, Long> lastUpdates = new HashMap<>();

    public StrikeUpdateDatabaseSQLite() {
        ResultSet resultSet = LiteSQL.onQuery("SELECT * FROM strikeupdates");
        assert resultSet != null;
        try {
            while (resultSet.next()) {
                lastUpdates.put(resultSet.getLong("strikeid"), resultSet.getLong("lastupdate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getLastHandledUpdate(Long strikeId) {
        return lastUpdates.get(strikeId);
    }

    @Override
    public void setLastHandledUpdate(Long strikeId, Long timestamp) {
        if (!lastUpdates.containsKey(strikeId)) {
            LiteSQL.onUpdate("INSERT INTO strikeupdates (strikeid, lastupdate) VALUES (" + strikeId + ", " + timestamp + ");");
        } else {
            LiteSQL.onUpdate("UPDATE strikeupdates SET lastupdate=" + timestamp + " WHERE strikeid=" + strikeId + ";");
        }
        lastUpdates.put(strikeId, timestamp);
    }

    @Override
    public void cleanupDatabase() {
        List<Long> allStrikeIds = new Strikes().getStrikes()
                .stream()
                .map(Strike::getId)
                .collect(Collectors.toList());
        lastUpdates.keySet()
                .stream()
                .filter(id -> !allStrikeIds.contains(id))
                .collect(Collectors.toList())
                .forEach(id -> {
                    lastUpdates.remove(id);
                    LiteSQL.onUpdate("DELETE FROM lastupdates WHERE strikeid=" + id + ";");
                });
    }
}
