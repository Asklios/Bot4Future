package main.java.temp;

import main.java.files.LiteSQL;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class UnMuteBanTask extends Thread {

	String type;
	long executeTime;
	long userId;
	Guild guild;
	Member member;
	String[] roleIds;
	long sqlId;
	ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
	
	public UnMuteBanTask(long sqlId, String type, long executeTime, long userId, Member member, Guild guild, String[] roleIds) {
		this.sqlId = sqlId;
		this.type = type;
		this.executeTime = executeTime;
		this.userId = userId;
		this.guild = guild;
		this.member = member;
		this.roleIds = roleIds;
	}
	
	@Override
	public void run() {
		if(executeTime <= System.currentTimeMillis()) {
			execute();
		} else {
			try {
				Thread.sleep(executeTime - System.currentTimeMillis());
				execute();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void execute() {
		if(type.equals("mute")) {
			GuildDatabase guildDataBase = new GuildDatabaseSQLite();
			try {
				guild.removeRoleFromMember(member, guildDataBase.getMuteRole(guild)).queue();
				for(String s: roleIds) {
	            	guild.addRoleToMember(member, guild.getRoleById(s)).queue();
	            }
				LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE id = " + sqlId);
				channelDatabase.getAuditChannel(guild).sendMessage("Der Mute von " + member.getAsMention() + " ist abgelaufen.").queue();
			} catch(NullPointerException e) {
				try {
					LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE id = " + sqlId);
					channelDatabase.getAuditChannel(guild).sendMessage("Der Mute von " + member.getAsMention() + " ist abgelaufen.").queue();
				}
				catch (NullPointerException f) {
					//f.printStackTrace();
				}
			}
			catch (IllegalArgumentException e) {
				System.out.println("Could not remove mute role from missing member. (" + member.getId() + ")");
			}
		} else {
			try {
				guild.unban("" + userId).queue();
				LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE id = " + sqlId);
				channelDatabase.getAuditChannel(guild).sendMessage("Der Tempban von " + member.getAsMention() + " ist abgelaufen.").queue();
			} catch(ErrorResponseException e1) {
				LiteSQL.onUpdate("UPDATE userrecords SET note = 'lifted' WHERE id = " + sqlId);
				channelDatabase.getAuditChannel(guild).sendMessage("Der Tempban von " + member.getAsMention() + " ist abgelaufen.").queue();
			} catch(NullPointerException e3) {
				//
			}
		}
	}
	
}
