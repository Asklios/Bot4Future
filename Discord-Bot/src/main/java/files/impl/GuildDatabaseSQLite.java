package main.java.files.impl;

import main.java.files.LiteSQL;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildDatabaseSQLite implements GuildDatabase {

    @Override
    public void startUpEntries(Guild guild) throws NullPointerException {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT * FROM guildroles WHERE guildid = " + guildid);

        try {
            if (result.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        LiteSQL.onUpdate("INSERT INTO guildroles(guildid, type) VALUES(" + guildid + ", 'mute')");
        LiteSQL.onUpdate("INSERT INTO guildroles(guildid, type) VALUES(" + guildid + ", 'specialrole')");
        LiteSQL.onUpdate("INSERT INTO guildroles(guildid, type) VALUES(" + guildid + ", 'verifiablerole')");
        LiteSQL.onUpdate("INSERT INTO guildroles(guildid, type) VALUES(" + guildid + ", 'specialcode')");
    }

    @Override
    public void setMuteRole(Guild guild, Role role) throws NullPointerException{
        long guildid = guild.getIdLong();
        long roleid = role.getIdLong();

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleid + " WHERE guildid = " + guildid + " AND type = 'mute'");

    }

    @Override
    public Role getMuteRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'mute'");

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");
                Role muteRole = guild.getRoleById(muteRoleId);
                result.close();
                return muteRole;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void setSpecialRole(Guild guild, Role role) throws NullPointerException {
        long guildid = guild.getIdLong();
        long roleid = role.getIdLong();

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleid + " WHERE guildid = " + guildid + " AND type = 'specialrole'");
    }

    @Override
    public Role getSpecialRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'specialrole'");

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");
                Role muteRole = guild.getRoleById(muteRoleId);

                return muteRole;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void setVerifyRole(Guild guild, Role role) throws NullPointerException {
        long guildid = guild.getIdLong();
        long roleid = role.getIdLong();

        LiteSQL.onUpdate("UPDATE guildroles SET roleid = " + roleid + " WHERE guildid = " + guildid + " AND type = 'verifiablerole'");
    }

    @Override
    public Role getVerifyRole(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT roleid FROM guildroles WHERE guildid = " + guildid + " AND type = 'verifiablerole'");

        try {
            if (result.next()) {
                long muteRoleId = result.getLong("roleid");
                Role muteRole = guild.getRoleById(muteRoleId);

                return muteRole;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void setSpecialCode(Guild guild, String code) throws NullPointerException {
        long guildid = guild.getIdLong();

        LiteSQL.onUpdate("UPDATE guildroles SET code = '" + code + "' WHERE guildid = " + guildid + " AND type = 'specialcode'");
    }

    @Override
    public String getSpecialCode(Guild guild) {
        long guildid = guild.getIdLong();

        ResultSet result = LiteSQL.onQuery("SELECT code FROM guildroles WHERE guildid = " + guildid + " AND type = 'specialcode'");

        try {
            if (result.next()) {
                String code = result.getString("code");

                return code;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}
