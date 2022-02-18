package dev.nincodedo.ocwgameservercommander;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

@Component
public class GameServerCommand {

    private final GameServerRepository gameServerRepository;
    private final GameServerManager gameServerManager;

    public GameServerCommand(GameServerManager gameServerManager, GameServerRepository gameServerRepository) {
        this.gameServerRepository = gameServerRepository;
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
        var gameServer = gameServerRepository.findGameServerByName(gameOption.getAsString());
        if (!gameServer.isOnline()) {
            gameServerManager.startGameServer(gameServer);
            event.getHook().editOriginalFormat("Starting %s", gameServer.getName()).queue();
        } else {
            event.getHook().editOriginalFormat("%s is already started.", gameServer.getName()).queue();
        }
    }

    private void fixGameServer(SlashCommandInteractionEvent event) {
        var gameOption = event.getOption("game");
        if (gameOption == null) {
            event.reply("???").setEphemeral(true).queue();
            return;
        }
        var game = gameOption.getAsString();
        event.reply("Nincodedo has been notified and will look into ASAP.").setEphemeral(true).queue();
        var gameServer = gameServerRepository.findGameServerByName(game);
        var ninPrivateMessageChannel = event.getJDA().openPrivateChannelById("86958766125244416").complete();

        ninPrivateMessageChannel.sendMessageFormat("Game server %s is reported as broken by %s",
                        gameServer.getName(), event.getUser().getName())
                .setActionRow(Button.danger(String.format("gsc-restart-%s", gameServer.getId()), String.format("Restart %s?", gameServer.getName())),
                        Button.secondary("gsc-ignore", "Disable fix alerts"))
                .queue();
    }

    private void listGameServers(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        var gameServers = gameServerRepository.findAll();
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
            var gameServers = gameServerRepository.findAll();
            if (!gameServers.isEmpty()) {
                var gameNameList = gameServers.stream().map(GameServer::getName).toList();
                event.replyChoiceStrings(gameNameList).queue();
            }
        }
    }

    public void executeButtonPress(ButtonInteractionEvent event, String buttonId) {
        var buttonAction = getButtonAction(buttonId);
        if (buttonAction.actionName().equals("restart")) {
            event.deferEdit().queue();
            gameServerRepository.findById(Long.valueOf(buttonAction.value()));
            event.editButton(event.getButton().asDisabled().withLabel("Restarting...")).queue();

        } else if (buttonAction.actionName().equals("ignore")) {
            //TODO add disable alert option for u
            event.deferEdit().queue();
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
