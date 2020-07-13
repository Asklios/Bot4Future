package de.java.main;

import java.util.concurrent.ConcurrentHashMap;

import de.java.main.commands.AdminHelpCommand;
import de.java.main.commands.AuditChannelCommand;
import de.java.main.commands.ClearCommand;
import de.java.main.commands.GetAuditChannelCommand;
import de.java.main.commands.GetSpecialCodeCommand;
import de.java.main.commands.GetSpecialCodeRoleCommand;
import de.java.main.commands.HelpCommand;
import de.java.main.commands.KillCommand;
import de.java.main.commands.ReactCommand;
import de.java.main.commands.ReactRemoveCommand;
import de.java.main.commands.RoleCreation;
import de.java.main.commands.ServerCommand;
import de.java.main.commands.SpecialCodeCommand;
import de.java.main.commands.SpecialCodeRoleCommand;
import de.java.main.commands.UserInfoCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message;

public class CommandManager {
	
	public ConcurrentHashMap<String, ServerCommand> commands;
	
	public CommandManager() {
		this.commands = new ConcurrentHashMap<>();
		
		this.commands.put("clear", new ClearCommand());
		this.commands.put("specialcode", new SpecialCodeCommand());
		this.commands.put("getspecialcode", new GetSpecialCodeCommand());
		this.commands.put("specialrole", new SpecialCodeRoleCommand());
		this.commands.put("getspecialrole", new GetSpecialCodeRoleCommand());
		this.commands.put("createrole", new RoleCreation());
		this.commands.put("react", new ReactCommand());
		this.commands.put("unreact", new ReactRemoveCommand());
		this.commands.put("help", new HelpCommand());
		this.commands.put("info", new HelpCommand());
		this.commands.put("adminhelp", new AdminHelpCommand());
		this.commands.put("userinfo", new UserInfoCommand());
		this.commands.put("audit", new AuditChannelCommand());
		this.commands.put("getaudit", new GetAuditChannelCommand());
		this.commands.put("killbot4future", new KillCommand());
		//this.commands.put("eventaudit", new EventAuditChannelCommand());
		//this.commands.put("geteventaudit", new GetEventAuditChannelCommand());
		
	}
	
	public boolean perform(String command,Member m, TextChannel channel, Message message) {

		ServerCommand cmd;
		if((cmd = this.commands.get(command.toLowerCase())) !=null) { // cmd wird auf output von HashMap gesetzt wenn get abgefragt wird
			cmd.performCommand(m, channel, message);
			return true;
		}
		
		return false; // wenn cmd = null return false
	}
}

