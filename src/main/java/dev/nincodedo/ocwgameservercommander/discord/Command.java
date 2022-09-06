package dev.nincodedo.ocwgameservercommander.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Command {
    String getName();

    void executeSlashCommand(SlashCommandInteractionEvent event);
}
