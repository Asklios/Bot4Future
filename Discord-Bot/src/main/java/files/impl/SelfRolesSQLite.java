package main.java.files.impl;

import main.java.DiscordBot;
import main.java.commands.server.user.IAmCommand;
import main.java.files.LiteSQL;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.entities.Guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                } else {
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

        if (IAmCommand.getServerSelfRoles() == null) {
            loadSelfRoles();
        }

        HashMap<Long, HashMap<String, Long>> serverSelfRoles = IAmCommand.getServerSelfRoles();
        HashMap<String, Long> guildSelfRoles = serverSelfRoles.getOrDefault(guildId, new HashMap<>());
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

    @Override
    public void cleanUp() {
        Map<Long, HashMap<String, Long>> selfRoles = IAmCommand.getServerSelfRoles();
        int cnt = 0;
        for (long guildId : selfRoles.keySet()) {
            Guild guild = DiscordBot.INSTANCE.jda.getGuildById(guildId);
            if (guild == null) {
                List<Long> toRemove = new ArrayList<>();
                for (long roleId : selfRoles.get(guildId).values()) {
                    toRemove.add(roleId);
                    cnt++;
                }
                toRemove.forEach(id -> removeSelfRoleByRoleId(guildId, id));
            } else {
                List<Long> toRemove = new ArrayList<>();
                for (long id : selfRoles.get(guildId).values()) {
                    if (guild.getRoleById(id) == null) {
                        toRemove.add(id);
                        cnt++;
                    }
                }
                toRemove.forEach(id -> removeSelfRoleByRoleId(guildId, id));
            }
        }

        System.out.println("Removed " + cnt +" selfrole" + (cnt == 1 ? "." : "s."));
    }
}
