package main.java.commands.server;

import main.java.commands.server.administation.*;
import main.java.commands.server.administation.presence.PresenceCommand;
import main.java.commands.server.administation.presence.RemoveCallDataCommand;
import main.java.commands.server.administation.presence.RequestCallDatabase;
import main.java.commands.server.audit.*;
import main.java.commands.server.bump.BumpRoleCommand;
import main.java.commands.server.bump.GetBumpRoleCommand;
import main.java.commands.server.developer.*;
import main.java.commands.server.developer.KillCommand;
import main.java.commands.server.invite.*;
import main.java.commands.server.pmCommands.GetPmChannelCommand;
import main.java.commands.server.pmCommands.PmChannelCommand;
import main.java.commands.server.user.*;
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
        this.commands.put("getbumprole", new GetBumpRoleCommand());
        this.commands.put("bumprole", new BumpRoleCommand());

        //functions
        this.commands.put("createrole", new RoleCreation());
        this.commands.put("sortroles", new RoleSortCommand());
        this.commands.put("react", new ReactCommand());
        this.commands.put("unreact", new ReactRemoveCommand());
        this.commands.put("reactionrole", new ReactRolesCommand());
        this.commands.put("userinfo", new UserInfoCommand());
        this.commands.put("presence", new PresenceCommand());
        this.commands.put("getpresence", new RequestCallDatabase());
        this.commands.put("removeallcalldata", new RemoveCallDataCommand());
        this.commands.put("yesiwanttodeleteallserverdata", new DeleteServerDataCommand());

        // moderation
        this.commands.put("log", new LogCommand());
        this.commands.put("clear", new ClearCommand());
        this.commands.put("delete", new ClearCommand());
        this.commands.put("clearuser", new ClearMessagesFromUserCommand());
        this.commands.put("slow", new SlowModeCommand());
        this.commands.put("slowend", new RemoveSlowModeCommand());
        this.commands.put("warn", new WarnCommand());
        this.commands.put("mute", new MuteCommand());
        this.commands.put("ban", new BanCommand());
        this.commands.put("banid", new BanIdCommand());
        this.commands.put("tempban", new TempBanCommand());

        //settings
        this.commands.put("muterole", new MuteRoleCommand());
        this.commands.put("getmuterole", new GetMuteRoleCommand());
        this.commands.put("newmuterole", new NewMuteRoleCommand());
        this.commands.put("audit", new AuditChannelCommand());
        this.commands.put("getaudit", new GetAuditChannelCommand());
        this.commands.put("eventaudit", new EventAuditChannelCommand());
        this.commands.put("geteventaudit", new GetEventAuditChannelCommand());
        this.commands.put("auditignore", new EventAuditIgnoredChannelsCommand());
        this.commands.put("pmchannel", new PmChannelCommand());
        this.commands.put("getpmchannel", new GetPmChannelCommand());
        this.commands.put("selfrole", new AddSelfRoleCommand());
        this.commands.put("rmselfrole", new RemoveSelfRoleCommand());
        /*this.commands.put("poll", new PollCommand());
        this.commands.put("getpoll", new GetPollCommand());
        this.commands.put("closepoll", new ClosePollCommand());*/

        //user
        this.commands.put("report", new ReportCommand());
        this.commands.put("pb", new ProfilePictureGeneratorCommand());
        this.commands.put("og", new OgInfoCommand());
        this.commands.put("ogs", new ListOgsCommand());
        this.commands.put("iam", new IAmCommand());

        // bot info
        this.commands.put("help", new HelpCommand());
        this.commands.put("info", new HelpCommand());
        this.commands.put("adminhelp", new AdminHelpCommand());

        //dev
        this.commands.put("killbot4future", new KillCommand());
        this.commands.put("getdb", new GetDbCommand());
        this.commands.put("dbclear", new DbClearCommand());
        this.commands.put("getguilds", new GetGuildsCommand());
        this.commands.put("devtest", new DevelopmentTestCommand());
    }

    public boolean perform(String command, Member m, TextChannel channel, Message message) {

        ServerCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            cmd.performCommand(m, channel, message);
            return true;
        }
        return false;
    }
}

