package main.java.commands.slash.models;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class SelfrolesSlashCommand extends CommandData {
    public SelfrolesSlashCommand() {
        super("selfroles", "Zum Verwalten der Selbstgebbaren Rollen.");
        setDefaultEnabled(false);
        addSubcommands(new AddSelfrole(), new RemoveSelfrole());
    }

    private class AddSelfrole extends SubcommandData {
        public AddSelfrole() {
            super("add", "Füge eine Rolle zu den selbst gebbaren Rollen hinzu.");
            addOption(OptionType.ROLE, "rolle", "Die Rolle, die hinzugefügt werden soll.", true);
        }
    }

    private class RemoveSelfrole extends SubcommandData {
        public RemoveSelfrole() {
            super("remove", "Entferne eine Rolle von den selbst gebbaren Rollen.");
            addOption(OptionType.ROLE, "rolle", "Die Rolle, die entfernt werden soll.", true);
        }
    }

}
