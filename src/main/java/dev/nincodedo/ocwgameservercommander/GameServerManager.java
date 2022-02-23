package dev.nincodedo.ocwgameservercommander;

import dev.nincodedo.ocwgameservercommander.common.ContainerUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameServerManager {
    private final GameServerService gameServerService;
    private final ContainerUtil containerUtil;
    @Getter
    @Setter
    private boolean recentChangesMade = false;

    public GameServerManager(GameServerService gameServerService, ContainerUtil containerUtil) {
        this.gameServerService = gameServerService;
        this.containerUtil = containerUtil;
    }

    public void startGameServer(GameServer gameServer) {
        log.trace("Attempting to start {}", gameServer);
        var gameContainers = containerUtil.getGameContainerByName(gameServer.getName());
        log.trace("Found {} game container(s)", gameContainers.size());
        containerUtil.startContainers(gameContainers);
        gameServer.setOnline(true);
        gameServerService.save(gameServer);
    }

    public void stopGameServer(GameServer gameServer) {
        log.trace("Attempting to stop {}", gameServer);
        var gameContainers = containerUtil.getGameContainerByName(gameServer.getName());
        log.trace("Found {} game container(s)", gameContainers.size());
        containerUtil.stopContainers(gameContainers);
        gameServer.setOnline(false);
        gameServerService.save(gameServer);
    }

    public String getGameServerLogs(GameServer gameServer) {
        var gameContainers = containerUtil.getGameContainerByName(gameServer.getName());
        return containerUtil.getLogsForContainers(gameContainers);
    }

    public void restartGameServer(GameServer gameServer) {

    }
}
