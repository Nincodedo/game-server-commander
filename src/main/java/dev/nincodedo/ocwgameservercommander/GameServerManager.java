package dev.nincodedo.ocwgameservercommander;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameServerManager {
    public static final String GSC_GAME_NAME_KEY = "dev.nincodedo.gameservercommander.name";
    private final GameServerService gameServerService;
    private final CommonContainerUtil commonContainerUtil;
    @Getter
    @Setter
    private boolean recentChangesMade = false;

    public GameServerManager(GameServerService gameServerService, CommonContainerUtil commonContainerUtil) {
        this.gameServerService = gameServerService;
        this.commonContainerUtil = commonContainerUtil;
    }

    public void startGameServer(GameServer gameServer) {
        log.trace("Attempting to start {}", gameServer);
        var gameContainers = commonContainerUtil.getGameContainerByName(gameServer.getName());
        log.trace("Found {} game container(s)", gameContainers.size());
        commonContainerUtil.startContainers(gameContainers);
        gameServer.setOnline(true);
        gameServerService.save(gameServer);
    }

    public void stopGameServer(GameServer gameServer) {
        log.trace("Attempting to stop {}", gameServer);
        var gameContainers = commonContainerUtil.getGameContainerByName(gameServer.getName());
        log.trace("Found {} game container(s)", gameContainers.size());
        commonContainerUtil.stopContainers(gameContainers);
        gameServer.setOnline(false);
        gameServerService.save(gameServer);
    }

    public String getGameServerLogs(GameServer gameServer) {
        var gameContainers = commonContainerUtil.getGameContainerByName(gameServer.getName());
        return commonContainerUtil.getLogsForContainers(gameContainers);
    }

    public void restartGameServer(GameServer gameServer) {

    }
}
