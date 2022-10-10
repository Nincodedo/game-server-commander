package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.model.Container;
import dev.nincodedo.ocwgameservercommander.common.ContainerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GameServerDbSync {
    private final GameServerService gameServerService;
    private final ContainerUtil containerUtil;

    public GameServerDbSync(GameServerService gameServerService, ContainerUtil containerUtil) {
        this.gameServerService = gameServerService;
        this.containerUtil = containerUtil;
        updateStatuses();
        addNewGameServers();
        logCounts();
    }

    private void logCounts() {
        var total = gameServerService.findAll().size();
        var totalServers = total == 1 ? "server" : "servers";
        var online = gameServerService.getOnlineGameServerCount();
        var onlineServers = online == 1 ? "server" : "servers";
        log.trace("Initial startup complete, {} {} total, {} {} online", total, totalServers, online, onlineServers);
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    public void addNewGameServers() {
        var gameContainers = containerUtil.getAllGameServerContainers()
                .stream()
                .collect(Collectors.toMap(container -> container.getLabels()
                        .get(ContainerUtil.GSC_NAME_KEY), Container::getId));
        var allGameServers = gameServerService.findAll();
        var currentGameList = allGameServers.stream().map(GameServer::getName).toList();
        for (var gameName : gameContainers.keySet()) {
            if (!currentGameList.contains(gameName)) {
                log.trace("Found new game server for {}, adding", gameName);
                addNewGameServer(gameName);
            }
        }
        for (var gameServer : allGameServers) {
            var optionalContainer = containerUtil.getContainerById(gameServer.getContainerId());
            if (optionalContainer.isEmpty()) {
                log.trace("Game server {} in DB does not exist as a container with id {}, removing", gameServer, gameServer.getContainerId());
                gameServerService.delete(gameServer);
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
        log.trace("Added new game server: {}", gameServer);
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
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
