package main.java.commands.invite;

import java.util.concurrent.TimeUnit;

import main.java.commands.ServerCommand;
import main.java.files.GuildDataXmlReadWrite;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetVerifiableRoleCommand implements ServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		if(member.hasPermission(channel, Permission.ADMINISTRATOR)) {
		
			try {
				
				long verifiableRoleId = GuildDataXmlReadWrite.readVerifiableRole(member.getGuild().getIdLong());
				if(verifiableRoleId != 0) {
					String roleMention = message.getGuild().getRoleById(verifiableRoleId).getAsMention();
					channel.sendMessage(roleMention + " ist die aktuelle verifiableRole.").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				} else {
					channel.sendMessage("Die aktuelle verifiableRole ist nicht festgelegt.\n```%verifiablerole @role```").complete().delete().queueAfter(10, TimeUnit.SECONDS);
				}
				
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				System.err.println("Cought Exception: NumberFormatException (GetVerifiableRoleCommand.java - performCommand)");
			}
		} else {
			channel.sendMessage(member.getAsMention() + " Du hast nicht die Berechtigung diesen Befehl zu nutzen :(").complete().delete().queueAfter(10, TimeUnit.SECONDS);
		}
	}
}
