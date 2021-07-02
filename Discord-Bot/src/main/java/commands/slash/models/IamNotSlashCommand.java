package main.java.commands.slash.models;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class IamNotSlashCommand extends CommandData {
    public IamNotSlashCommand() {
        super("iamnot", "Entferne bei dir eine selbst gebbare Rollen.");
        this.addOption(OptionType.ROLE, "rolle", "Die Rolle die du entfernen willst.", true);
    }
}
