package main.java.files.impl;

import main.java.DiscordBot;
import main.java.files.LiteSQL;
import main.java.files.interfaces.RoleDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for updating and requesting the guildroles table from the database.
 *
 * @author Asklios
 * @version 03.12.2020
 */
public class RoleDatabaseSQLite implements RoleDatabase {

    private static String[] roleTypes = new String[]{
            "mute",
            "specialrole",
            "specialcode",
            "verifiablerole",
            "bumprole"
    };

    /**
     * Checks if there are lines for this guild in the table and adds them if not.
     *
     * @param guild that should be checked.
     */
    @Override
    public void startUpEntries(Guild guild) throws NullPointerException {
        startUpEntries(guild.getIdLong());
    }

    /**
     * Checks if there are lines for this guild in the table and adds them if not.
     *
     * @param guildId the id from the guild that should be checked.
     */
    @Override
    public void startUpEntries(long guildId) throws NullPointerException {
        ResultSet result = LiteSQL.onQuery("SELECT * FROM guildroles WHERE guildid = " + guildId);
        List<String> existingTypes = new ArrayList<>();
        try {
            assert result != null;
            if (result.next()) {
                exsitingTypes.add(result.getString("type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(String type : roleTypes){
            if(!exsitingTypes.contains(type)) LiteSQL.onUpdate("INSERT INTO guildroles(guildid, type) VALUES(" + guildId + ", '" + type + "')");
        }
    }

    /**
     * Sets the mute role for the provided guild to a new value.
     *
     * @param guild the guild.
     * @param role  the new mute role.
     */
    @Override
    public void setMuteRole(Guild guild, Role role) throws NullPointerException {
        long guildId = guild.getIdLong();
        long roleId = role.getIdLong();
        String type = "mute";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Sets the mute role for the provided guild to a new value.
     *
     * @param guild  the guild.
     * @param roleId the id of the new mute role.
     */
    @Override
    public void setMuteRole(Guild guild, long roleId) throws NullPointerException {
        long guildId = guild.getIdLong();
        String type = "mute";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Returns the current mute role from the provided guild.
     *
     * @param guild the guild.
     * @return The saved mute role. Can be null.
     */
    @Override
    public Role getMuteRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'mute'");
        if (result == null) return null;

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");
                return guild.getRoleById(muteRoleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the special role for the provided guild to a new value.
     *
     * @param guild the guild.
     * @param role  the new specialRole.
     */
    @Override
    public void setSpecialRole(Guild guild, Role role) throws NullPointerException {
        long guildId = guild.getIdLong();
        long roleId = role.getIdLong();
        String type = "specialrole";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Sets the special role for the provided guild to a new value.
     *
     * @param guild  the guild.
     * @param roleId the id of the new specialRole.
     */
    @Override
    public void setSpecialRole(Guild guild, long roleId) throws NullPointerException {
        long guildId = guild.getIdLong();
        String type = "specialrole";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Returns the current special role from the provided guild.
     *
     * @param guild the guild.
     * @return The saved specialRole.
     */
    @Override
    public Role getSpecialRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'specialrole'");
        if (result == null) return null;

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");

                return guild.getRoleById(muteRoleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the verifiable role for the provided guild to a new value.
     *
     * @param guild the guild.
     * @param role  the new verifiableRole.
     */
    @Override
    public void setVerifyRole(Guild guild, Role role) throws NullPointerException {
        long guildId = guild.getIdLong();
        long roleId = role.getIdLong();
        String type = "verifiablerole";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Sets the verifiable role for the provided guild to a new value.
     *
     * @param guild  the guild.
     * @param roleId the id of the new verifiableRole.
     */
    @Override
    public void setVerifyRole(Guild guild, long roleId) throws NullPointerException {
        long guildId = guild.getIdLong();
        String type = "verifiablerole";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleId + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }

    /**
     * Returns the current verifiable role from the provided guild.
     *
     * @param guild the guild.
     * @return The saved specialRole.
     */
    @Override
    public Role getVerifyRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'verifiablerole'");
        if (result == null) return null;

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");

                return guild.getRoleById(muteRoleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets the special invite code for the provided guild to a new value.
     *
     * @param guild the guild.
     * @param code  the invite-code.
     */
    @Override
    public void setSpecialCode(Guild guild, String code) throws NullPointerException {
        long guildid = guild.getIdLong();

        LiteSQL.onUpdate("UPDATE guildroles SET code = '" + code + "' WHERE guildid = " + guildid + " AND type = 'specialcode'");
    }

    /**
     * Returns the current special invite-code from the provided guild.
     *
     * @param guild the guild.
     * @return The saved invite-code.
     */
    @Override
    public String getSpecialCode(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT code FROM guildroles WHERE guildid = " + guildid + " AND type = 'specialcode'");
        if (result == null) return null;

        try {
            if (result.next()) {
                return result.getString("code");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes all entries where the guild was left.
     *
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean removeUnusedEntries() {
        JDA jda = DiscordBot.INSTANCE.jda;
        ArrayList<Long> guildIds = new ArrayList<>();
        jda.getGuilds().forEach(guild -> guildIds.add(guild.getIdLong()));

        Map<Integer, Long> resultMap = new HashMap<>();
        ResultSet result = LiteSQL.onQuery("SELECT id, guildid FROM guildroles");

        if (result == null) return false;

        try {
            while (result.next()) {
                int id = result.getInt("id");
                long guildId = result.getLong("guildid");

                resultMap.put(id, guildId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        ArrayList<Integer> removeIds = new ArrayList<>();

        resultMap.forEach((id, guildId) -> {
            if (!guildIds.contains(guildId)) removeIds.add(id);
        });

        removeIds.forEach(id -> LiteSQL.onUpdate("DELETE FROM guildroles WHERE id = " + id));
        return true;
    }

    /**
     * Removes all entries that are connected to the provided guild.
     *
     * @param guild The guild whose entries should be deleted.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean removeEntriesByGuildId(Guild guild) {
        long guildId = guild.getIdLong();

        LiteSQL.onUpdate("DELETE FROM guildroles WHERE guildid = " + guildId);
        startUpEntries(guild);
        return true;
    }

    /**
     * Removes all entries that are connected to the provided guild.
     *
     * @param guildId The id of the guild whose entries should be deleted.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean removeEntriesByGuildId(long guildId) {
        LiteSQL.onUpdate("DELETE FROM guildroles WHERE guildid = " + guildId);
        startUpEntries(guildId);
        return true;
    }

    @Override
    public Role getBumpRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'bumprole'");
        if (result == null) return null;

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");

                return guild.getRoleById(muteRoleId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setBumpRole(Guild guild, Role role) {
        long guildId = guild.getIdLong();
        String type = "bumprole";

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + role.getId() + " WHERE guildid = " + guildId + " AND type = '" + type + "'");
    }
}
