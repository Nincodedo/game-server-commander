package dev.nincodedo.ocwgameservercommander.discord;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CommandListener extends ListenerAdapter {

    private final GameServerCommand gameServerCommand;
    private final ExecutorService executorService;

    public CommandListener(GameServerCommand gameServerCommand) {
        this.gameServerCommand = gameServerCommand;
        this.executorService = Executors.newCachedThreadPool(new NamedThreadFactory("command-listener"));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("games") || event.getSubcommandName() == null) {
            return;
        }
        executorService.execute(() -> gameServerCommand.executeSlashCommand(event));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() != null && event.getButton().getId().startsWith("gsc-")) {
            executorService.execute(() -> gameServerCommand.executeButtonPress(event, event.getButton().getId()));
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("games") || event.getSubcommandName() == null) {
            return;
        }
        executorService.execute(() -> gameServerCommand.executeAutoComplete(event));
    }
}
