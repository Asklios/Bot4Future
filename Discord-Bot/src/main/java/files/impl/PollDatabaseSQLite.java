package main.java.files.impl;

import main.java.DiscordBot;
import main.java.files.LiteSQL;
import main.java.files.interfaces.PollDatabase;
import main.java.util.Poll;
import main.java.util.PollChoice;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PollDatabaseSQLite implements PollDatabase {
    private final List<Poll> polls = new ArrayList<>();

    @Override
    public void loadAllPolls() {
        try {
            polls.clear();
            Statement stmt1 = LiteSQL.getStatement();

            ResultSet resultSet = stmt1.executeQuery("SELECT * FROM polls;");

            if (resultSet == null) {
                System.out.println("--- ERROR: UMFRAGEN KONNTEN NICHT GELADEN WERDEN! ---");
                return;
            }

            while (resultSet.next()) {
                PollImpl poll = new PollImpl();
                poll.name = resultSet.getString("name");
                poll.description = resultSet.getString("description");
                poll.closeTime = resultSet.getLong("endtime");
                poll.closeDisplay = DiscordBot.FORMATTER.print(new DateTime(poll.closeTime));
                poll.userId = resultSet.getString("ownerid");
                poll.guildId = resultSet.getString("guildid");
                poll.msgId = resultSet.getString("msgid");
                poll.votesPerUser = resultSet.getInt("votesperuser");
                poll.showVotes = resultSet.getInt("hidevotes") != 1;
                Statement stmt2 = LiteSQL.getStatement();
                ResultSet choiceResult = stmt2.executeQuery("SELECT * FROM pollchoices WHERE pollguildid=" + poll.guildId + " AND pollmsgid=" + poll.msgId);
                while (choiceResult.next()) {
                    PollChoiceImpl choice = new PollChoiceImpl();
                    choice.text = choiceResult.getString("value");
                    choice.choiceId = choiceResult.getInt("choiceid");
                    Statement stmt3 = LiteSQL.getStatement();
                    ResultSet voteResult = stmt3.executeQuery("SELECT * FROM pollvotes WHERE pollguildid=" + poll.guildId + " AND pollmsgid=" + poll.msgId + " AND choiceid=" + choice.choiceId);
                    while (voteResult.next()) {
                        choice.votes.add(voteResult.getString("userid"));
                    }
                    stmt3.close();
                    voteResult.close();
                    poll.choices.add(choice);
                }
                choiceResult.close();
                stmt2.close();
                Collections.sort(poll.choices);
                polls.add(poll);
            }
            resultSet.close();
            stmt1.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<Poll> getPolls() {
        return polls;
    }

    @Override
    public Poll getPoll(String id) {
        return polls.stream().filter(poll -> poll.getMessageId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void deletePoll(String id) {
        LiteSQL.onUpdate("DELETE FROM polls WHERE msgid = " + id);
        if (getPoll(id) != null) {
            polls.remove(getPoll(id));
        }
    }

    @Override
    public void savePoll(Poll poll) throws SQLException {
        PreparedStatement stmt = LiteSQL.prepStmt("INSERT INTO polls (guildid, msgid, name, description, votesperuser, endtime, ownerid, hidevotes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        stmt.setString(1, poll.getGuildId());
        stmt.setString(2, poll.getMessageId());
        stmt.setString(3, poll.getName());
        stmt.setString(4, poll.getDescription());
        stmt.setInt(5, poll.getVotesPerUser());
        stmt.setLong(6, poll.getCloseTime());
        stmt.setString(7, poll.getPollOwner());
        stmt.setInt(8, poll.areVotesVisible() ? 0 : 1);

        PreparedStatement stmt2 = LiteSQL.prepStmt("INSERT INTO pollchoices (pollguildid, pollmsgid, choiceid, value) VALUES (?, ?, ?, ?)");
        for (PollChoice c : poll.getChoices()) {
            stmt2.setString(1, poll.getGuildId());
            stmt2.setString(2, poll.getMessageId());
            stmt2.setInt(3, c.getChoiceId());
            stmt2.setString(4, c.getText());
            stmt2.executeUpdate();
        }

        stmt2.close();
        stmt.executeUpdate();
        stmt.close();
        polls.add(poll);
    }

    @Override
    public void saveVotes() throws SQLException {
        LiteSQL.onUpdate("DELETE FROM pollvotes");
        Statement stmt = LiteSQL.getStatement();
        assert stmt != null;
        polls.forEach(p -> {
            p.getChoices().forEach(choice -> {
                choice.getVotes().forEach(user -> {
                    try {
                        stmt.addBatch("INSERT INTO pollvotes (pollguildid, pollmsgid, choiceid, userid) VALUES (" + p.getGuildId() + ", " + p.getMessageId() + ", " + choice.getChoiceId() + ", " + user + ")");
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
            });
        });
        stmt.executeBatch();
        stmt.close();
    }


    private class PollImpl implements Poll {
        public String name;
        public String description;
        public String guildId;
        public String msgId;
        public String userId;
        public int votesPerUser = 1;
        public long closeTime = 0;
        public String closeDisplay;
        public boolean showVotes = true;
        List<PollChoice> choices = new ArrayList<>();

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public List<PollChoice> getChoices() {
            return choices;
        }

        @Override
        public String getGuildId() {
            return guildId;
        }

        @Override
        public String getMessageId() {
            return msgId;
        }

        @Override
        public int getVotesPerUser() {
            return votesPerUser;
        }

        @Override
        public long getCloseTime() {
            return closeTime;
        }

        @Override
        public String getCloseDisplay() {
            return closeDisplay;
        }

        @Override
        public String getPollOwner() {
            return userId;
        }

        @Override
        public boolean areVotesVisible() {
            return showVotes;
        }
    }

    private class PollChoiceImpl implements PollChoice {
        public int choiceId = -1;
        public List<String> votes = new ArrayList<>();
        public String text = null;

        @Override
        public int getChoiceId() {
            return choiceId;
        }

        @Override
        public List<String> getVotes() {
            return votes;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}