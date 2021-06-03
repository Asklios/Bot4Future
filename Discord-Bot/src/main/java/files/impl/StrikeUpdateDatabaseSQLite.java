package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.StrikeUpdateDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class StrikeUpdateDatabaseSQLite implements StrikeUpdateDatabase {
    private Map<Long, Long> lastUpdates = new HashMap<>();

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
}
