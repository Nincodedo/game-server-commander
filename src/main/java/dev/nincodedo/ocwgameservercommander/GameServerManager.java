package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GameServerManager {
    private final GameServerService gameServerService;
    private final DockerClient dockerClient;
    public static final String GSC_GAME_NAME_KEY = "dev.nincodedo.gameservercommander.name";
    public static final String GCS_GROUP_KEY = "dev.nincodedo.gameservercommander.group";

    public GameServerManager(GameServerService gameServerService, DockerClient dockerClient) {
        this.gameServerService = gameServerService;
        this.dockerClient = dockerClient;
    }

    public void startGameServer(GameServer gameServer) {
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
            gameServer.setOnline(true);
            gameServerService.save(gameServer);
        }
    }
}
