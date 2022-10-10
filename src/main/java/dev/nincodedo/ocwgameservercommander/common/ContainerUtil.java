package dev.nincodedo.ocwgameservercommander.common;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ContainerUtil {
    public static final String GCS_GROUP_KEY = "dev.nincodedo.gameservercommander.group";
    public static final String GSC_NAME_KEY = "dev.nincodedo.gameservercommander.name";
    public static final String GSC_GAME_KEY = "dev.nincodedo.gameservercommander.game";
    public static final String GSC_DESCRIPTION_KEY = "dev.nincodedo.gameservercommander.description";

    private final DockerClient dockerClient;

    public ContainerUtil(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public @NotNull List<Container> getGameContainerByName(String gameServerName) {
        var gameContainerList = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> gameServerName.equalsIgnoreCase(container.getLabels()
                        .get(GSC_NAME_KEY)))
                .toList();
        if (gameContainerList.isEmpty()) {
            return new ArrayList<>();
        }
        var gameContainer = gameContainerList.get(0);
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

    public List<Container> getAllGameServerContainers() {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> container.getLabels().containsKey(GSC_NAME_KEY))
                .toList();
    }

    public void startContainer(Container container) {
        dockerClient.startContainerCmd(container.getId()).exec();
    }

    public void startContainers(List<Container> gameContainers) {
        log.trace("Starting potentially {} container(s)", gameContainers.size());
        gameContainers.stream().filter(container -> !container.getState().equals("running"))
                .forEach(container -> {
                    log.trace("Starting container {}", container.getId());
                    startContainer(container);
                });
    }

    public void stopContainers(List<Container> gameContainers) {
        gameContainers.stream().filter(container -> container.getState().equals("running"))
                .forEach(container -> {
                    log.trace("Stopping container {}", container.getId());
                    stopContainer(container);
                });
    }

    public void stopContainer(Container container) {
        dockerClient.stopContainerCmd(container.getId()).exec();
    }

    public String getLogsForContainers(List<Container> gameContainers) {


        dockerClient.logContainerCmd(gameContainers.get(0).getId()).start();

        return null;
    }

    public Container getMainGameContainerByName(String gameName) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> gameName.equalsIgnoreCase(container.getLabels()
                        .get(GSC_NAME_KEY)))
                .toList().get(0);
    }

    public Optional<Container> getContainerById(String containerId) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> container.getId().equals(containerId))
                .findFirst();
    }
}
