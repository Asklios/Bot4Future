package main.java.listener;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import main.java.files.LiteSQL;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		
		if (event.getChannelType() == ChannelType.TEXT) {
				
			if (!event.getUser().isBot()) {
				
				long guildID = event.getGuild().getIdLong();
				long channelID = event.getChannel().getIdLong();
				long messageID = event.getMessageIdLong();
				String emote = event.getReactionEmote().getEmoji();
				
				ResultSet set = LiteSQL.onQuery("SELECT roleid FROM reactroles WHERE guildid = " +
				guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " And emote = '" + emote + "'");
				
				try {
					if (set.next()) {
						long roleID = set.getLong("roleid");
						
						Guild guild = event.getGuild();
						
						guild.addRoleToMember(event.getMember(), guild.getRoleById(roleID)).queue();
						guild.getTextChannelById(channelID).sendTyping();
					}
				}
				catch (SQLException |NullPointerException e) {
					e.printStackTrace();
				}
				catch (HierarchyException e) {
					event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht vergeben, da sie höher als die Bot-Rolle ist.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				}
				catch (IllegalArgumentException e) {
					event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht vergeben, da sie nicht mehr existiert.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				}
			}
		}
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		
		if (event.getChannelType() == ChannelType.TEXT) {
				
			if (!event.getUser().isBot()) {
				
				long guildID = event.getGuild().getIdLong();
				long channelID = event.getChannel().getIdLong();
				long messageID = event.getMessageIdLong();
				String emote = event.getReactionEmote().getEmoji();
				
				ResultSet set = LiteSQL.onQuery("SELECT roleid FROM reactroles WHERE guildid = " +
				guildID + " AND channelid = " + channelID + " AND messageid = " + messageID + " And emote = '" + emote + "'");
				
				try {
					if (set.next()) {
						long roleID = set.getLong("roleid");
						
						Guild guild = event.getGuild();
						
						guild.removeRoleFromMember(event.getMember(), guild.getRoleById(roleID)).queue();
						guild.getTextChannelById(channelID).sendTyping();
					}
				}
				catch (SQLException | NullPointerException e) {
					e.printStackTrace();
				}
				catch (HierarchyException e) {
					event.getGuild().getTextChannelById(channelID).sendMessage("Der Bot kann die Rolle nicht entziehen, da sie höher als die Bot-Rolle ist.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
				}
				catch (IllegalArgumentException e) {
					// Reaktion von einer gelöschten Rolle wird entfernt
				}
			}
		}
	}
}
