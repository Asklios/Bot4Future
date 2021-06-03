package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.StrikeUpdateDatabase;
import main.java.files.interfaces.SubscribtionDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SubscribtionDatabaseSQLite implements SubscribtionDatabase {
    private Map<Long, List<String>> subscribtions = new HashMap<>();
    private StrikeUpdateDatabase updateDB;

    public SubscribtionDatabaseSQLite() {
        updateDB = new StrikeUpdateDatabaseSQLite();
        ResultSet resultSet = LiteSQL.onQuery("SELECT * FROM subscribtions;");
        assert resultSet != null;
        try {
            while (resultSet.next()) {
                Long ogId = resultSet.getLong("localgroupid");
                String userId = resultSet.getString("userid");
                if (!subscribtions.containsKey(ogId)) subscribtions.put(ogId, new ArrayList<>());
                subscribtions.get(ogId).add(userId);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public StrikeUpdateDatabase getUpdateDatabase() {
        return updateDB;
    }

    @Override
    public List<Long> getSubscribtionsOfUser(String id) {
        List<Long> subs = new ArrayList<>();
        subscribtions.forEach((l, list) -> {
            if (list.contains(id)) subs.add(l);
        });

        return subs;
    }

    @Override
    public void subscribe(String userId, Long ogId) {
        if (subscribtions.containsKey(ogId) && subscribtions.get(ogId).contains(userId)) return;
        if (!subscribtions.containsKey(ogId)) subscribtions.put(ogId, new ArrayList<>());
        subscribtions.get(ogId).add(userId);
        LiteSQL.onUpdate("INSERT INTO subscribtions (userid, localgroupid) VALUES (\"" + userId + "\", " + ogId + ");");
    }

    @Override
    public void unsubscribe(String userId, Long ogId) {
        if (subscribtions.containsKey(ogId) && subscribtions.get(ogId).contains(userId))
            subscribtions.get(ogId).remove(userId);
        LiteSQL.onUpdate("DELETE FROM subscribtions WHERE userid=\"" + userId + "\" AND localgroupid=\"" + ogId + "\";");
    }

    @Override
    public Map<Long, List<String>> getAllSubscribtions() {
        return Collections.unmodifiableMap(subscribtions);
    }
}
