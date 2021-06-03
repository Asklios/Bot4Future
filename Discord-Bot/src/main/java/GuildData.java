package main.java;

public class GuildData {

    private final long id;
    private String specialInviteCode;
    private long specialRoleID;

    public GuildData(long id) {
        this.id = id;
    }

    public long getID() {
        return id;
    }


    public String getSpecialInviteCode() {
        return specialInviteCode;
    }

    public void setSpecialInviteCode(String newCode) {
        specialInviteCode = newCode;
    }

    public long getSpecialRoleID() {
        return specialRoleID;
    }

    public void setSpecialRoleID(long newID) {
        specialRoleID = newID;
    }
}
