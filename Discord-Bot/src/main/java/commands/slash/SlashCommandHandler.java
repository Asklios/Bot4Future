package main.java.commands.slash;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

@FunctionalInterface
public interface SlashCommandHandler {
    void handle(SlashCommandEvent event);
}
