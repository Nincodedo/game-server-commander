package dev.nincodedo.ocwgameservercommander;

import com.github.dockerjava.api.model.Container;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GameServerStatusChecker {
    private final GameServerService gameServerService;
    private final CommonContainerUtil commonContainerUtil;

    public GameServerStatusChecker(GameServerService gameServerService, CommonContainerUtil commonContainerUtil) {
        this.gameServerService = gameServerService;
        this.commonContainerUtil = commonContainerUtil;
        updateStatuses();
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void updateStatuses() {
        gameServerService.findAll().forEach(gameServer -> {
            var containers = commonContainerUtil.getGameContainerByName(gameServer.getName());
            var actualStatus = allContainersOnline(containers);
            if (gameServer.isOnline() != actualStatus) {
                gameServer.setOnline(actualStatus);
                gameServerService.save(gameServer);
            }
        });
    }

    private boolean allContainersOnline(List<Container> containers) {
        for (var container : containers) {
            if (container.getState().equals("exited")) {
                return false;
            }
        }
        return true;
    }


}
