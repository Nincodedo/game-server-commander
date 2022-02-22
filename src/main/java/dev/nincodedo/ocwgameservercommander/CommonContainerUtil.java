package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CommonContainerUtil {
    public static final String GCS_GROUP_KEY = "dev.nincodedo.gameservercommander.group";

    private final DockerClient dockerClient;

    public CommonContainerUtil(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public @NotNull List<Container> getGameContainerByName(String gameServerName) {
        var gameContainer = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> gameServerName.equalsIgnoreCase(container.getLabels()
                        .get(GameServerManager.GSC_GAME_NAME_KEY)))
                .toList().get(0);
        var containerList = new ArrayList<Container>();
        if (gameContainer.getLabels().containsKey(GCS_GROUP_KEY)) {
            var groupName = gameContainer.getLabels().get(GCS_GROUP_KEY);
            containerList.addAll(getAllContainers().stream()
                    .filter(container -> groupName.equalsIgnoreCase(container.getLabels().get(GCS_GROUP_KEY)))
                    .toList());
            log.trace("Additional containers found within group {}, adding {}", groupName, containerList.size());
        } else {
            containerList.add(gameContainer);
            log.trace("No additional containers found");
        }
        return containerList;
    }

    public List<Container> getAllContainers() {
        return dockerClient.listContainersCmd().withShowAll(true).exec();
    }

    public void startContainer(Container container) {
        dockerClient.startContainerCmd(container.getId()).exec();
    }

    public void startContainers(List<Container> gameContainers) {
        log.trace("Starting potentially {} container(s)", gameContainers.size());
        gameContainers.stream().filter(container -> container.getState().equals("exited"))
                .forEach(container -> {
                    log.trace("Starting container {}", container.getId());
                    startContainer(container);
                });
    }
}