package main.java.temp;

import main.java.DiscordBot;
import main.java.files.LiteSQL;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;

public class UnMuteBanCheck extends TimerTask {
	
	boolean isDefinitelyMuted = false;
	
	public void setDefinitelyMuted(boolean isDefinitelyMuted) {
		this.isDefinitelyMuted = isDefinitelyMuted;
	}

	public void run() {
		System.out.println("Unmute/ban loop");
		ResultSet results = LiteSQL.onQuery("SELECT id, userid, endtime, guildid, note, type FROM userrecords WHERE note != 'lifted' AND (type = 'mute' OR type = 'tempban')");
		GuildDatabase roleDatabase = new GuildDatabaseSQLite();
		try {
			while (results.next()) {
				
				int id = results.getInt("id");
				long userId = results.getLong("userid");
				long endTime = results.getLong("endtime");
				long guildId = results.getLong("guildId");
				String note = results.getString("note");
				String type = results.getString("type");
                String[] roleIds = note.split("⫠");
				
                Guild guild = DiscordBot.INSTANCE.shardMan.getGuildById(guildId);
                Member member = null;
                if(type.equals("mute")) {
					try {
						member = guild.getMemberById(userId);
					} catch(NullPointerException e) {
					}
                }
				//set already unmuted members to lifted
				if(type.equals("mute") && member != null && !isMuted(member, roleDatabase.getMuteRole(guild)) && !isDefinitelyMuted) {
					LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE id = " + id);
					continue;
				}
				
				if(isDefinitelyMuted) {
					isDefinitelyMuted = false;
				}
				
				if(System.currentTimeMillis() < endTime) {
					if(System.currentTimeMillis() + DiscordBot.INSTANCE.getMuteTimerPeriod() > endTime) {
						 UnMuteBanTask task = new UnMuteBanTask(id, type, endTime, userId, member, guild, roleIds);
						 task.start();
					}
					continue;
				} else {
					UnMuteBanTask task = new UnMuteBanTask(id, type, endTime, userId, member, guild, roleIds);
					task.execute();
				}
			}
		} catch (SQLException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	boolean isMuted(Member member, Role muteRole) {
		List<Role> roles = member.getRoles();
		for(Role r: roles) {
			if(r.getIdLong() == muteRole.getIdLong()) {
				return true;
			}
		}
		return false;
	}
}

