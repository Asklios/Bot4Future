package main.java.files.impl;

import main.java.commands.server.user.IAmCommand;
import main.java.files.LiteSQL;
import main.java.files.interfaces.SelfRoles;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

public class SelfRolesSQLite implements SelfRoles {

    @Override
    public void loadSelfRoles() {
        HashMap<Long, HashMap<String, Long>> serverRoles = new HashMap<>();

        PreparedStatement prepStmt = LiteSQL.prepStmt("SELECT * FROM selfroles");
        ResultSet result = null;
        try {
            assert prepStmt != null;
            result = prepStmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null) return;

        try {
            while (result.next()) {
                long guildId = result.getLong("guildid");
                String role = result.getString("role");
                long roleId = result.getLong("roleid");

                if (serverRoles.containsKey(guildId)) {
                    serverRoles.get(guildId).put(role.toLowerCase(), roleId);
                }
                else {
                    HashMap<String, Long> roleMap = new HashMap<>();
                    roleMap.put(role.toLowerCase(), roleId);
                    serverRoles.put(guildId, roleMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        IAmCommand.setServerSelfRoles(serverRoles);
    }

    @Override
    public void addSelfRole(long guildId, String role, long roleId) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("INSERT INTO selfroles(guildid, role, roleid) VALUES(?,?,?)");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, guildId);
            prepStmt.setString(2, role.toLowerCase());
            prepStmt.setLong(3, roleId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HashMap<Long, HashMap<String, Long>> serverSelfRoles = IAmCommand.getServerSelfRoles();
        HashMap<String, Long> guildSelfRoles = serverSelfRoles.get(guildId);
        guildSelfRoles.put(role.toLowerCase(), roleId);
        serverSelfRoles.put(guildId, guildSelfRoles);
        IAmCommand.setServerSelfRoles(serverSelfRoles);
    }

    @Override
    public void removeSelfRoleByRoleId(long guildId, long roleId) {
        PreparedStatement prepStmt = LiteSQL.prepStmt("DELETE FROM selfroles WHERE guildid = ? AND roleid = ?");
        try {
            assert prepStmt != null;
            prepStmt.setLong(1, guildId);
            prepStmt.setLong(2, roleId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HashMap<Long, HashMap<String, Long>> serverSelfRoles = IAmCommand.getServerSelfRoles();
        HashMap<String, Long> guildSelfRoles = serverSelfRoles.get(guildId);
        String roleName = null;
        for (Entry<String, Long> e : guildSelfRoles.entrySet()) {
            if (e.getValue().equals(roleId)) {
                roleName = e.getKey();
                break;
            }
        }

        assert roleName != null;
        guildSelfRoles.remove(roleName.toLowerCase());
        serverSelfRoles.put(guildId, guildSelfRoles);
        IAmCommand.setServerSelfRoles(serverSelfRoles);
    }
}
