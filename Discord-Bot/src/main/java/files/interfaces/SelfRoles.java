package main.java.files.interfaces;

public interface SelfRoles {

    void loadSelfRoles();

    void addSelfRole(long guildId, String role, long roleId);

    void removeSelfRoleByRoleId(long guildId, long roleId);
}
