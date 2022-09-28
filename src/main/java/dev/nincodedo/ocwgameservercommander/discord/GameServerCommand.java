package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.GameServer;
import dev.nincodedo.ocwgameservercommander.GameServerManager;
import dev.nincodedo.ocwgameservercommander.GameServerService;
import dev.nincodedo.ocwgameservercommander.config.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GameServerCommand implements Command {

    private final GameServerService gameServerService;
    private final GameServerManager gameServerManager;
    private final Constants constants;

    public GameServerCommand(GameServerManager gameServerManager, GameServerService gameServerService, Constants constants) {
        this.gameServerService = gameServerService;
        this.gameServerManager = gameServerManager;
        this.constants = constants;
    }

    public void executeSlashCommand(SlashCommandInteractionEvent event) {
        if ("start".equals(event.getSubcommandName())) {
            startGameServer(event);
        } else if ("list".equals(event.getSubcommandName())) {
            listGameServers(event);
        } else if ("fix".equals(event.getSubcommandName())) {
            fixGameServer(event);
        }
    }

    private void startGameServer(SlashCommandInteractionEvent event) {
        var gameServerName = event.getOption("game", OptionMapping::getAsString);
        var result = gameServerManager.startGameServer(gameServerName);
        switch (result) {
            case STARTING -> {
                event.deferReply().queue();
                event.getHook().editOriginalFormat("Starting %s...", gameServerName).queue(message -> {
                    var started = gameServerManager.waitForGameServerStart(gameServerName);
                    if (started) {
                        message.editMessageFormat("%s has started.", gameServerName).queue();
                        message.addReaction(Emoji.fromFormatted("✅")).queue();
                    } else {
                        message.editMessageFormat("%s failed to start. Use '/games fix' to notify %s.", gameServerName, constants.gameServerAdminName())
                                .queue();
                        message.addReaction(Emoji.fromFormatted("❌")).queue();
                    }
                });
            }
            case ALREADY_STARTED ->
                    event.replyFormat("%s is already started. If you think this server may be broken, use '/games fix' to notify %s.", gameServerName, constants.gameServerAdminName())
                            .setEphemeral(true)
                            .queue();
            case NOT_FOUND ->
                    event.replyFormat("Could not find server named %s.", gameServerName).setEphemeral(true).queue();
            default -> throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    private void fixGameServer(SlashCommandInteractionEvent event) {
        var gameServerName = event.getOption("game", OptionMapping::getAsString);
        event.replyFormat("%s has been notified and will look into the issue ASAP.", constants.gameServerAdminName())
                .setEphemeral(true)
                .queue();
        gameServerService.findGameServerByName(gameServerName)
                .ifPresentOrElse(gameServer -> event.getJDA()
                        .openPrivateChannelById(constants.gameServerAdminId())
                        .complete()
                        .sendMessageFormat("Game server %s is reported as broken by %s.", gameServer.getName(), event.getUser()
                                .getName())
                        .setActionRow(Button.danger(String.format("gsc-restart-%s", gameServer.getId()), String.format("Restart %s?", gameServer.getName())), Button.danger(String.format("gsc-stop-%s", gameServer.getId()), String.format("Stop %s?", gameServer.getName())), Button.secondary("gsc-ignore", "Disable fix alerts"))
                        .queue(), () -> event.getHook()
                        .editOriginalFormat("Could not find server named %s.", gameServerName)
                        .queue());
    }

    private void listGameServers(SlashCommandInteractionEvent event) {
        var gameServers = gameServerService.findAll();
        if (gameServers.isEmpty()) {
            event.replyFormat("No game servers found. Contact %s.", constants.gameServerAdminName()).setEphemeral(true).queue();
        } else {
            event.deferReply().queue();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("OCW Game Servers");
            gameServers.forEach(gameServer -> embedBuilder.addField(gameServer.getGameTitle(), gameServer.getGameDescription(), true));
            event.getHook().editOriginal(new MessageEditBuilder().setEmbeds(embedBuilder.build()).build()).queue();
        }
    }

    public void executeAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if ("start".equals(event.getSubcommandName()) || "fix".equals(event.getSubcommandName())) {
            var gameServers = gameServerService.findAll();
            if (!gameServers.isEmpty()) {
                var gameNameList = gameServers.stream().map(GameServer::getName).limit(OptionData.MAX_CHOICES).toList();
                event.replyChoiceStrings(gameNameList).queue();
            } else {
                event.replyChoiceStrings(Collections.emptyList()).queue();
            }
        }
    }

    public void executeButtonPress(ButtonInteractionEvent event, String buttonId) {
        if (!event.getUser().getId().equals(constants.gameServerAdminId())) {
            return;
        }
        var buttonAction = getButtonAction(buttonId);
        switch (buttonAction.actionName()) {
            case "restart" -> {
                event.deferEdit().queue();
                gameServerService.findById(Long.valueOf(buttonAction.value())).ifPresentOrElse(gameServer -> {
                    event.editButton(event.getButton().asDisabled().withLabel("Restarting...")).queue();
                    gameServerManager.restartGameServer(gameServer);
                }, () -> event.editButton(event.getButton().asDisabled().withLabel("Failed to restart")).queue());
            }
            case "stop" -> {
                event.deferEdit().queue();
                gameServerService.findById(Long.valueOf(buttonAction.value())).ifPresentOrElse(gameServer -> {
                    event.editButton(event.getButton().asDisabled().withLabel("Stopping...")).queue();
                    gameServerManager.stopGameServer(gameServer);
                }, () -> event.editButton(event.getButton().withLabel("Failed to stop")).queue());
            }
            //TODO add disable alert option for u
            case "ignore" -> event.deferEdit().queue();
            default -> event.reply("???").queue();
        }
    }

    private ButtonAction getButtonAction(String buttonId) {
        var button = buttonId.split("-");
        String prefix = button.length >= 1 ? button[0] : null;
        String actionName = button.length >= 2 ? button[1] : null;
        String value = button.length >= 3 ? button[2] : null;
        return new ButtonAction(prefix, actionName, value);
    }

    @Override
    public String getName() {
        return "games";
    }

    public record ButtonAction(String prefix, String actionName, String value) {
    }
}
