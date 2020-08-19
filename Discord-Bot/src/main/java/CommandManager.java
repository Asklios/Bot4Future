package main.java;

import java.util.concurrent.ConcurrentHashMap;

import main.java.commands.ServerCommand;
import main.java.commands.administation.AdminHelpCommand;
import main.java.commands.administation.BanCommand;
import main.java.commands.administation.ClearCommand;
import main.java.commands.administation.HelpCommand;
import main.java.commands.administation.KillCommand;
import main.java.commands.administation.LogCommand;
import main.java.commands.administation.ReactCommand;
import main.java.commands.administation.ReactRemoveCommand;
import main.java.commands.administation.ReportCommand;
import main.java.commands.administation.RoleCreation;
import main.java.commands.administation.UserInfoCommand;
import main.java.commands.audit.AuditChannelCommand;
import main.java.commands.audit.GetAuditChannelCommand;
import main.java.commands.invite.GetSpecialCodeCommand;
import main.java.commands.invite.GetSpecialCodeRoleCommand;
import main.java.commands.invite.GetVerifiableRoleCommand;
import main.java.commands.invite.SpecialCodeCommand;
import main.java.commands.invite.SpecialCodeRoleCommand;
import main.java.commands.invite.VerifiableRoleCommand;
import main.java.commands.invite.VerifyCommand;
import main.java.commands.pnSystem.GetPnChannelCommand;
import main.java.commands.pnSystem.PnChannelCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message;

public class CommandManager {
	
	public ConcurrentHashMap<String, ServerCommand> commands;
	
	public CommandManager() {
		this.commands = new ConcurrentHashMap<>();
		
		
		
		// invite manager
		this.commands.put("specialcode", new SpecialCodeCommand());
		this.commands.put("getspecialcode", new GetSpecialCodeCommand());
		this.commands.put("specialrole", new SpecialCodeRoleCommand());
		this.commands.put("getspecialrole", new GetSpecialCodeRoleCommand());
		this.commands.put("verify", new VerifyCommand());
		this.commands.put("verifiablerole", new VerifiableRoleCommand());
		this.commands.put("getverifiablerole", new GetVerifiableRoleCommand());
		
		// moderation
		this.commands.put("clear", new ClearCommand());
		this.commands.put("createrole", new RoleCreation());
		this.commands.put("react", new ReactCommand());
		this.commands.put("unreact", new ReactRemoveCommand());
		this.commands.put("userinfo", new UserInfoCommand());
		this.commands.put("killbot4future", new KillCommand());
		this.commands.put("ban", new BanCommand());
		this.commands.put("audit", new AuditChannelCommand());
		this.commands.put("getaudit", new GetAuditChannelCommand());
		//this.commands.put("eventaudit", new EventAuditChannelCommand());
		//this.commands.put("geteventaudit", new GetEventAuditChannelCommand());
		this.commands.put("pnchannel", new PnChannelCommand());
		this.commands.put("getpnchannel", new GetPnChannelCommand());
		this.commands.put("log", new LogCommand());
		this.commands.put("report", new ReportCommand());
		
		// bot info
		this.commands.put("help", new HelpCommand());
		this.commands.put("info", new HelpCommand());
		this.commands.put("adminhelp", new AdminHelpCommand());
		
		
		
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

