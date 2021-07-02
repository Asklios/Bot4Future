package main.java.commands.slash.models;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class ListSelfrolesCommand extends CommandData {
    public ListSelfrolesCommand() {
        super("listroles", "Zeigt dir alle selbst gebbaren Rollen an.");
    }
}
