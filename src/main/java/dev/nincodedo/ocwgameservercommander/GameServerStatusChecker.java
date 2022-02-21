package dev.nincodedo.ocwgameservercommander;

import org.springframework.stereotype.Component;

@Component
public class GameServerStatusChecker {
    private final GameServerService gameServerService;

    public GameServerStatusChecker(GameServerService gameServerService){
        this.gameServerService = gameServerService;
    }


}
