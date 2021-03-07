package main.java.commands.server.pmCommands;

public class UnbanRequest {

    private long userID;
    private long guildID;
    private String reason;


    public UnbanRequest(long userID) {
        this.userID = userID;
    }

    public void setGuildID(long guildID) {
        this.guildID = guildID;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getUserID() {
        return userID;
    }

    public long getGuildID() {
        return guildID;
    }

    public String getReason() {
        return reason;
    }

}
