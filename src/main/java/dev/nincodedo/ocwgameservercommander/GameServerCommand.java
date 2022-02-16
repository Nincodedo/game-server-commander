package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GameServerCommand {

    public static final String GSC_GAME_NAME_KEY = "dev.nincodedo.gameservercommander.name";
    public static final String GCS_GROUP_KEY = "dev.nincodedo.gameservercommander.group";
    private final GameServerRepository gameServerRepository;
    private final DockerClient dockerClient;

    public GameServerCommand(GameServerRepository gameServerRepository, DockerClient dockerClient) {
        this.gameServerRepository = gameServerRepository;
        this.dockerClient = dockerClient;
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
            var allContainers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec()
                    .stream()
                    .filter(container -> gameServer.getName().equalsIgnoreCase(container.getLabels().get(GSC_GAME_NAME_KEY)))
                    .toList();
            if (allContainers.size() == 1) {
                var gameContainer = allContainers.get(0);
                var containerList = new ArrayList<Container>();
                if (gameContainer.getLabels().containsKey(GCS_GROUP_KEY)) {
                    var groupName = gameContainer.getLabels().get(GCS_GROUP_KEY);
                    containerList.addAll(dockerClient.listContainersCmd().withShowAll(true).exec().stream()
                            .filter(container -> groupName.equalsIgnoreCase(container.getLabels().get(GCS_GROUP_KEY)))
                            .toList());
                } else {
                    containerList.add(gameContainer);
                }
                containerList.stream().filter(container -> container.getState().equals("exited")).forEach(container -> dockerClient.startContainerCmd(container.getId()).exec());
                event.getHook().editOriginal("Starting " + gameServer.getName()).queue();
            } else if (allContainers.isEmpty()) {
                event.getHook().editOriginal("Game server not found. Contact Nin.").queue();
            } else {
                event.getHook().editOriginal("Multiple games found???").queue();
            }
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

        ninPrivateMessageChannel.sendMessage(String.format("Game server %s is reported as broken by %s",
                        gameServer.getName(), event.getUser().getName()))
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
