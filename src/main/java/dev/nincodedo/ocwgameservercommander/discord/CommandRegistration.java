package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.config.Constants;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CommandRegistration extends ListenerAdapter {

    private final Constants constants;

    public CommandRegistration(Constants constants) {
        this.constants = constants;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        if (event.getJDA().getShardManager() == null) {
            return;
        }
        event.getJDA()
                .getShardManager()
                .getGuilds()
                .stream()
                .filter(guild -> guild.getOwnerId().equals(constants.gameServerAdminId()))
                .forEach(guild -> {
                    var gameServerCommandData = Commands.slash("games", "Main game server command.")
                            .addSubcommands(List.of(
                                    new SubcommandData("start", "Start a game server.")
                                            .addOption(OptionType.STRING, "game", "Name of the game server you want to start.", true, true),
                                    new SubcommandData("list", "List all game servers."),
                                    new SubcommandData("online", "List online game servers."),
                                    new SubcommandData("fix", "Mark a game server as broken and need fixing.")
                                            .addOption(OptionType.STRING, "game", "Name of the game server that needs fixing.", true, true),
                                    new SubcommandData("suggest", "Suggest a game that should be added.")
                                            .addOption(OptionType.STRING, "game", "Name of the game you're suggesting.", true)
                            ))
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
                    var adminServerCommand = Commands.slash("admin", "Admin commands for game servers.")
                            .setGuildOnly(true)
                            .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
                    guild.updateCommands().addCommands(gameServerCommandData, adminServerCommand).queue();
                });
    }
}
