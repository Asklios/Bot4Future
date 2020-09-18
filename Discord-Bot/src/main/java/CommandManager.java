package main.java;

import main.java.commands.ServerCommand;
import main.java.commands.administation.*;
import main.java.commands.audit.AuditChannelCommand;
import main.java.commands.audit.GetAuditChannelCommand;
import main.java.commands.developer.DbClearCommand;
import main.java.commands.developer.GetDbCommand;
import main.java.commands.developer.GetXmlCommand;
import main.java.commands.developer.KillCommand;
import main.java.commands.invite.*;
import main.java.commands.pnSystem.GetPnChannelCommand;
import main.java.commands.pnSystem.PnChannelCommand;
import main.java.commands.user.ProfilePictureGeneratorCommand;
import main.java.commands.user.ReportCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.ConcurrentHashMap;

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
        this.commands.put("delete", new ClearCommand());
        this.commands.put("createrole", new RoleCreation());
        this.commands.put("sortroles", new RoleSortCommand());
        this.commands.put("react", new ReactCommand());
        this.commands.put("unreact", new ReactRemoveCommand());
        this.commands.put("reactionrole", new ReactRolesCommand());
        this.commands.put("dbclear", new DbClearCommand());
        this.commands.put("userinfo", new UserInfoCommand());
        this.commands.put("warn", new WarnCommand());
        this.commands.put("mute", new MuteCommand());
        this.commands.put("muterole", new MuteRoleCommand());
        this.commands.put("getmuterole", new GetMuteRoleCommand());
        this.commands.put("ban", new BanCommand());
        this.commands.put("banid", new BanIdCommand());
        this.commands.put("tempban", new TempBanCommand());
        this.commands.put("audit", new AuditChannelCommand());
        this.commands.put("getaudit", new GetAuditChannelCommand());
        //this.commands.put("eventaudit", new EventAuditChannelCommand());
        //this.commands.put("geteventaudit", new GetEventAuditChannelCommand());
        this.commands.put("pnchannel", new PnChannelCommand());
        this.commands.put("getpnchannel", new GetPnChannelCommand());
        this.commands.put("log", new LogCommand());
        this.commands.put("poll", new PollCommand());
        this.commands.put("getpoll", new GetPollCommand());
        this.commands.put("closepoll", new ClosePollCommand());

        //user
        this.commands.put("report", new ReportCommand());
        this.commands.put("pb", new ProfilePictureGeneratorCommand());

        // bot info
        this.commands.put("help", new HelpCommand());
        this.commands.put("info", new HelpCommand());
        this.commands.put("adminhelp", new AdminHelpCommand());

        //dev
        this.commands.put("killbot4future", new KillCommand());
        this.commands.put("getxml", new GetXmlCommand());
        this.commands.put("getdb", new GetDbCommand());


    }

    public boolean perform(String command, Member m, TextChannel channel, Message message) {

        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) { // cmd wird auf output von HashMap gesetzt wenn get abgefragt wird
            cmd.performCommand(m, channel, message);
            return true;
        }

        return false; // wenn cmd = null return false
    }
}

