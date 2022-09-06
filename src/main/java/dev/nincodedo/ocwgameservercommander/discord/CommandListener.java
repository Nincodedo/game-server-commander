package dev.nincodedo.ocwgameservercommander.discord;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CommandListener extends ListenerAdapter {

    public static final String GAMES_COMMAND_NAME = "games";
    private final Map<String, Command> commandMap = new HashMap<>();
    private final ExecutorService executorService;

    public CommandListener(List<Command> commands) {
        this.executorService = Executors.newCachedThreadPool(new NamedThreadFactory("command-listener"));
        addCommands(commands);
    }

    private void addCommands(List<Command> commands) {
        commands.forEach(command -> commandMap.put(command.getName(), command));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var command = commandMap.get(event.getName());
        if (command == null || command.getName().equals(GAMES_COMMAND_NAME) && event.getSubcommandName() == null) {
            return;
        }
        executorService.execute(() -> command.executeSlashCommand(event));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() != null && event.getButton().getId().startsWith("gsc-")) {
            var command = (GameServerCommand) commandMap.get(GAMES_COMMAND_NAME);
            executorService.execute(() -> command.executeButtonPress(event, event.getButton().getId()));
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals(GAMES_COMMAND_NAME) || event.getSubcommandName() == null) {
            return;
        }
        var command = (GameServerCommand) commandMap.get(GAMES_COMMAND_NAME);
        executorService.execute(() -> command.executeAutoComplete(event));
    }
}
