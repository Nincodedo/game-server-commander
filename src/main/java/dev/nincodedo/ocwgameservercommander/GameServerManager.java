package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.model.Container;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
public class GameServerManager {
    private final GameServerService gameServerService;
    private final CommonContainerUtil commonContainerUtil;
    @Getter
    @Setter
    private boolean recentChangesMade = false;
    public static final String GSC_GAME_NAME_KEY = "dev.nincodedo.gameservercommander.name";

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

    public int getOnlineGameServerCount() {
        return gameServerService.getOnlineGameServerCount();
    }

    public void restartGameServer(GameServer gameServer) {

    }
}
