package dev.nincodedo.ocwgameservercommander;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandRegistration extends ListenerAdapter {

    @Value("${ocwServerId}")
    private String ocwServerId;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildById(ocwServerId)
                .upsertCommand(
                        Commands.slash("games", "Main game server command.")
                                .addSubcommands(List.of(
                                        new SubcommandData("start", "Start a game server.")
                                                .addOption(OptionType.STRING, "game", "Name of the game server you want to start.", true, true),
                                        new SubcommandData("list", "List available game servers."),
                                        new SubcommandData("fix", "Mark a game server as broken and need fixing.")
                                                .addOption(OptionType.STRING, "game", "Name of the game server that needs fixing.", true, true)
                                ))).queue();
    }
}
