package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.GameServer;
import dev.nincodedo.ocwgameservercommander.GameServerManager;
import dev.nincodedo.ocwgameservercommander.GameServerService;
import dev.nincodedo.ocwgameservercommander.config.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class GameServerCommand {

    private final GameServerService gameServerService;
    private final GameServerManager gameServerManager;
    private final Constants constants = new Constants();

    public GameServerCommand(GameServerManager gameServerManager, GameServerService gameServerService) {
        this.gameServerService = gameServerService;
        this.gameServerManager = gameServerManager;
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
        var gameOption = event.getOption("game");
        if (gameOption == null) {
            event.reply("???").setEphemeral(true).queue();
            return;
        }
        event.deferReply().queue();
        var optionalGameServer = gameServerService.findGameServerByName(gameOption.getAsString());
        optionalGameServer.ifPresentOrElse(gameServer -> {
            if (!gameServer.isOnline()) {
                gameServerManager.startGameServer(gameServer);
                event.getHook().editOriginalFormat("Starting %s...", gameServer.getName()).queue();

            } else {
                event.getHook().editOriginalFormat("%s is already started. If you think this server may be broken, " +
                        "use '/games fix' to notify Nincodedo.", gameServer.getName()).queue();
            }
        }, replyGameServerNotFound(event, gameOption.getAsString()));
    }

    @NotNull
    private Runnable replyGameServerNotFound(SlashCommandInteractionEvent event, String gameServerName) {
        return () -> event.getHook().editOriginalFormat("Could not find server named %s.", gameServerName).queue();
    }

    private void fixGameServer(SlashCommandInteractionEvent event) {
        var gameOption = event.getOption("game");
        if (gameOption == null) {
            event.reply("???").setEphemeral(true).queue();
            return;
        }
        var game = gameOption.getAsString();
        event.reply("Nincodedo has been notified and will look into the issue ASAP.").setEphemeral(true).queue();
        gameServerService.findGameServerByName(game).ifPresentOrElse(gameServer -> {
            event.getJDA().openPrivateChannelById(constants.getNincodedoUserId()).complete()
                    .sendMessageFormat("Game server %s is reported as broken by %s.", gameServer.getName(), event.getUser()
                            .getName())
                    .setActionRow(
                            Button.danger(String.format("gsc-restart-%s", gameServer.getId()), String.format("Restart %s?", gameServer.getName())),
                            Button.danger(String.format("gsc-stop-%s", gameServer.getId()), String.format("Stop %s?", gameServer.getName())),
                            Button.secondary("gsc-ignore", "Disable fix alerts"))
                    .queue();
        }, replyGameServerNotFound(event, gameOption.getAsString()));
    }

    private void listGameServers(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        var gameServers = gameServerService.findAll();
        if (gameServers.isEmpty()) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.append("No game servers found. Contact Nin.");
            event.getHook().editOriginal(messageBuilder.build()).queue();
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("OCW Game Servers");
            gameServers.forEach(gameServer -> embedBuilder.addField(gameServer.getGameTitle(), gameServer.getGameDescription(), false));
            event.getHook().editOriginal(new MessageBuilder(embedBuilder).build()).queue();
        }
    }

    public void executeAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if ("start".equals(event.getSubcommandName()) || "fix".equals(event.getSubcommandName())) {
            var gameServers = gameServerService.findAll();
            if (!gameServers.isEmpty()) {
                var gameNameList = gameServers.stream().map(GameServer::getName).toList();
                event.replyChoiceStrings(gameNameList).queue();
            }
        }
    }

    public void executeButtonPress(ButtonInteractionEvent event, String buttonId) {
        var buttonAction = getButtonAction(buttonId);
        switch (buttonAction.actionName()) {
            case "restart" -> {
                event.deferEdit().queue();
                gameServerService.findById(Long.valueOf(buttonAction.value())).ifPresentOrElse(gameServer -> {
                    event.editButton(event.getButton().asDisabled().withLabel("Restarting...")).queue();
                    gameServerManager.restartGameServer(gameServer);
                }, () -> event.editButton(event.getButton().withLabel("Failed to restart")).queue());
            }
            case "stop" -> {
                event.deferEdit().queue();
                gameServerService.findById(Long.valueOf(buttonAction.value())).ifPresentOrElse(gameServer -> {
                    event.editButton(event.getButton().asDisabled().withLabel("Stopping...")).queue();
                    gameServerManager.stopGameServer(gameServer);
                }, () -> event.editButton(event.getButton().withLabel("Failed to stop")).queue());
            }
            case "ignore" ->
                //TODO add disable alert option for u
                    event.deferEdit().queue();
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
}
