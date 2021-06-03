package main.java.commands.privateMessage;

import main.java.commands.privateMessage.info.ListOgsPmCommand;
import main.java.commands.privateMessage.info.OgInfoPmCommand;
import main.java.commands.privateMessage.info.PmInfoCommands;
import main.java.commands.privateMessage.pb.PbCountdownPmCommand;
import main.java.commands.privateMessage.pb.SetPbFilterCommand;
import main.java.commands.privateMessage.pb.SetPbPmCommand;
import main.java.commands.privateMessage.strikes.StrikeSubscribeCommand;
import main.java.commands.privateMessage.strikes.StrikeSubscribtionsCommand;
import main.java.commands.privateMessage.strikes.StrikeUnsubscribeCommand;
import main.java.commands.server.administation.poll.CreatePollCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.ConcurrentHashMap;

public class PrivateCommandManager {

    public ConcurrentHashMap<String, PrivateCommand> commands;

    public PrivateCommandManager() {
        this.commands = new ConcurrentHashMap<>();

        //PbCommands
        this.commands.put("setpbfilter", new SetPbFilterCommand());
        this.commands.put("setpb", new SetPbPmCommand());
        this.commands.put("pbcountdown", new PbCountdownPmCommand());
        this.commands.put("ogs", new ListOgsPmCommand());
        this.commands.put("og", new OgInfoPmCommand());

        this.commands.put("subscribe", new StrikeSubscribeCommand());
        this.commands.put("unsubscribe", new StrikeUnsubscribeCommand());
        this.commands.put("subscribtions", new StrikeSubscribtionsCommand());

        //PmInfoCommands
        String[] infoCommands = {"test", "webseite", "app", "instagram", "insta", "facebook", "twitter", "youtube",
                "discord", "ortsgruppen", "spenden", "hilfe", "commands", "info"};
        for (String s : infoCommands) {
            this.commands.put(s, new PmInfoCommands());
        }
    }

    public boolean perform(String command, User user, PrivateChannel channel, Message message) {

        PrivateCommand cmd;
        if ((cmd = this.commands.get(command.toLowerCase())) != null) {
            cmd.performCommand(user, channel, message);
            return true;
        }
        return false;
    }
}
