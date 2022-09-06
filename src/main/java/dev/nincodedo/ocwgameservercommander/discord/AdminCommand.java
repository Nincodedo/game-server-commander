package dev.nincodedo.ocwgameservercommander.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class AdminCommand implements Command{
    @Override
    public String getName() {
        return "admin";
    }

    @Override
    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        var subCommand = event.getSubcommandName();
    }
}
