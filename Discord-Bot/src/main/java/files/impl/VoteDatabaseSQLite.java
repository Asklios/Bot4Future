package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.VoteDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class VoteDatabaseSQLite implements VoteDatabase {

    //andere synchronized können nur von gleichem Thread ausgeführt werden

    @Override
    public synchronized void addVote(long guildID, long channelID, long messageID, long userID, String emote) throws Exception {

        ResultSet resultPoll = LiteSQL.onQuery("SELECT emotes, value, users FROM votereactions WHERE guildid = " +
                guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);

        if (resultPoll == null || !resultPoll.next()) {
            return;
        }
        String users = resultPoll.getString("users");
        String[] emoteString = resultPoll.getString("emotes").replaceAll(" ", "").split("⫠");
        Integer[] values = Arrays.stream(resultPoll.getString("value").split("⫠")).map(Integer::parseInt).toArray(Integer[]::new);

        for (int i = 0; i < values.length; i++) {
            if (emoteString[i].equals(emote)) {
                values[i] = values[i] +1;
            }
        }

        String newValue = Arrays.stream(values).map(Object::toString).collect(Collectors.joining("⫠"));

        users = users + "⫠" + userID;

        LiteSQL.onUpdate("UPDATE votereactions SET value = '" + newValue + "', users = '" + users + "' WHERE guildid = " +
                guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);


    }

    @Override
    public synchronized void removeVote(long guildID, long channelID, long messageID, long userID, String emote) throws Exception {

        ResultSet result = LiteSQL.onQuery("SELECT emotes, value, users FROM votereactions WHERE guildid = " +
                guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);
        if (result == null || !result.next()) {
            return;
        }
        String users = result.getString("users");
        String[] emoteString = result.getString("emotes").replaceAll(" ", "").split("⫠");
        Integer[] values = Arrays.stream(result.getString("value").split("⫠")).map(Integer::parseInt).toArray(Integer[]::new);

        for (int i = 0; i < values.length; i++) {
            if (emoteString[i].equals(emote)) {
                values[i] = Math.max(values[i] -1, 0);
            }
        }

        String newValue = Arrays.stream(values).map(Object::toString).collect(Collectors.joining("⫠"));

        users = users.replace("⫠" + userID,"");

        LiteSQL.onUpdate("UPDATE votereactions SET value = '" + newValue + "', users = '" + users + "' WHERE guildid = " +
                guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);
    }

    @Override
    public synchronized boolean hasVoted(long guildID, long channelID, long messageID, long userID) {
        try {
            return LiteSQL.onQuery("SELECT * FROM votereactions WHERE guildid = " +
                    guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " AND users LIKE '%" + userID + "%'").next() ;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public synchronized boolean isEmote(long guildID, long channelID, long messageID, String emote) {
        try {
            ResultSet result = LiteSQL.onQuery("SELECT * FROM votereactions WHERE guildid = " +
                    guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " AND emotes LIKE '%" + emote + "%'");
            if (result.next()) return true;
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
