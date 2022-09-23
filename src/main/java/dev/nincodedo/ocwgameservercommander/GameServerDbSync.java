package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.model.Container;
import dev.nincodedo.ocwgameservercommander.common.ContainerUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameServerDbSync {
    private final GameServerService gameServerService;
    private final ContainerUtil containerUtil;

    public GameServerDbSync(GameServerService gameServerService, ContainerUtil containerUtil) {
        this.gameServerService = gameServerService;
        this.containerUtil = containerUtil;
        updateStatuses();
        addNewGameServers();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void addNewGameServers() {
        var gameContainers = containerUtil.getAllGameServerContainers()
                .stream()
                .collect(Collectors.toMap(container -> container.getLabels()
                        .get(ContainerUtil.GSC_NAME_KEY), Container::getId));
        var currentGameList = gameServerService.findAll().stream().map(GameServer::getName).toList();
        for (var gameName : gameContainers.keySet()) {
            if (!currentGameList.contains(gameName)) {
                addNewGameServer(gameName);
            }
        }
    }

    private void addNewGameServer(String gameName) {
        var container = containerUtil.getMainGameContainerByName(gameName);
        var gameContainerLabels = container.getLabels();
        GameServer gameServer = new GameServer();
        gameServer.setContainerId(container.getId());
        gameServer.setName(gameContainerLabels.get(ContainerUtil.GSC_NAME_KEY));
        gameServer.setGame(gameContainerLabels.get(ContainerUtil.GSC_GAME_KEY));
        gameServer.setDescription(gameContainerLabels.get(ContainerUtil.GSC_DESCRIPTION_KEY));
        gameServer.setCreatedBy("GSC");
        gameServerService.save(gameServer);
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void updateStatuses() {
        gameServerService.findAll().forEach(gameServer -> {
            var containers = containerUtil.getGameContainerByName(gameServer.getName());
            var actualStatus = allContainersOnline(containers);
            if (gameServer.isOnline() != actualStatus) {
                gameServer.setOnline(actualStatus);
                gameServerService.save(gameServer);
            }
        });
    }

    private boolean allContainersOnline(List<Container> containers) {
        int count = 0;
        for (var container : containers) {
            if (container.getState().equals("running")) {
                count++;
            }
        }
        return count == containers.size();
    }
}
