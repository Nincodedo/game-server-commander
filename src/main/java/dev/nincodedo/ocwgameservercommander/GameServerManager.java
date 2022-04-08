package dev.nincodedo.ocwgameservercommander;

import dev.nincodedo.ocwgameservercommander.common.ContainerUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    public StartServerResult startGameServer(String gameServerName) {
        var optionalGameServer = gameServerService.findGameServerByName(gameServerName);
        if (optionalGameServer.isPresent()) {
            var gameServer = optionalGameServer.get();
            if (!gameServer.isOnline()) {
                startGameServer(gameServer);
                return StartServerResult.STARTING;
            } else {
                return StartServerResult.ALREADY_STARTED;
            }
        } else {
            return StartServerResult.NOT_FOUND;
        }
    }

    private void startGameServer(GameServer gameServer) {
        log.trace("Attempting to start {}", gameServer);
        var gameContainers = containerUtil.getGameContainerByName(gameServer.getName());
        log.trace("Found {} game container(s)", gameContainers.size());
        if (!gameContainers.isEmpty()) {
            containerUtil.startContainers(gameContainers);
            gameServer.setOnline(true);
            gameServerService.save(gameServer);
        }
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
        //TODO ^
    }

    public boolean waitForGameServerStart(String gameServerName) {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            while (true) {
                var status = containerUtil.getMainGameContainerByName(gameServerName).getStatus().toLowerCase();
                if(status.contains("healthy") || (!status.contains("health:") && status.contains("up"))) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            return future.get(5, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to wait for server", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
