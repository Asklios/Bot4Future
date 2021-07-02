package main.java.commands.slash.models;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class IamSlashCommand extends CommandData {
    public IamSlashCommand() {
        super("iam", "Gebe dir selbst gebbare Rollen.");
        this.addOption(OptionType.ROLE, "rolle", "Die Rolle die du dir geben willst.", true);
    }
}
