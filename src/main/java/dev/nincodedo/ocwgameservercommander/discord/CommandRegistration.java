package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.config.Constants;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandRegistration extends ListenerAdapter {

    private final Constants constants;

    public CommandRegistration(Constants constants){
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
                .filter(guild -> guild.getOwnerId().equals(constants.getNincodedoUserId()))
                .forEach(guild -> {
                    var gameServerCommandData = Commands.slash("games", "Main game server command.")
                            .addSubcommands(List.of(
                                    new SubcommandData("start", "Start a game server.")
                                            .addOption(OptionType.STRING, "game", "Name of the game server you want to start.", true, true),
                                    new SubcommandData("list", "List available game servers."),
                                    new SubcommandData("fix", "Mark a game server as broken and need fixing.")
                                            .addOption(OptionType.STRING, "game", "Name of the game server that needs fixing.", true, true)
                            )).setDefaultEnabled(false);
                    var gameServerCommand = guild.upsertCommand(gameServerCommandData).complete();
                    guild.getRolesByName("ocw", true).forEach(role ->
                            guild.updateCommandPrivilegesById(gameServerCommand.getId(), CommandPrivilege.enable(role))
                                    .queue());
                });
    }
}
