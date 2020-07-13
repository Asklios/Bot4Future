package de.java.main.commands;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import de.java.main.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class KillCommand implements ServerCommand{ 
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		if(Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(member.getId())) {
			message.delete().complete();
			DiscordBot.INSTANCE.shutdownCode();
		} else {
			channel.sendMessage(member.getAsMention() + " Dieser Command ist nur für die Botentwickler*innen vorgesehen.").complete().delete().queueAfter(5, TimeUnit.SECONDS);
		}
	}
}
