package dev.nincodedo.ocwgameservercommander;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class CommandListener extends ListenerAdapter {

    private final GameServerCommand gameServerCommand;

    public CommandListener(GameServerCommand gameServerCommand) {
        this.gameServerCommand = gameServerCommand;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("games") || event.getSubcommandName() == null) {
            return;
        }
        gameServerCommand.executeSlashCommand(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() != null && event.getButton().getId().startsWith("gsc-")) {
            gameServerCommand.executeButtonPress(event, event.getButton().getId());
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("games") || event.getSubcommandName() == null) {
            return;
        }
        gameServerCommand.executeAutoComplete(event);
    }
}
