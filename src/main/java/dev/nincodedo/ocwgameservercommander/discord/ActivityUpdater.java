package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.GameServerService;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityUpdater {

    private final GameServerService gameServerService;
    private final ShardManager shardManager;

    public ActivityUpdater(GameServerService gameServerService, ShardManager shardManager) {
        this.gameServerService = gameServerService;
        this.shardManager = shardManager;
        updateActivity();
    }

    @Scheduled(cron = "0 * * * * *")
    private void checkForUpdatedActivity() {
        if (gameServerService.isRecentChangesMade()) {
            updateActivity();
            gameServerService.acknowledgeRecentChangesMade();
        }
    }

    @Scheduled(cron = "0 */15 * * * *")
    private void updateActivity() {
        int serverCount = gameServerService.getOnlineGameServerCount();
        if (serverCount > 0) {
            String server = serverCount == 1 ? "server" : "servers";
            shardManager.setActivity(Activity.watching(String.format("%s game %s online", serverCount, server)));
        } else {
            shardManager.setActivity(Activity.listening("Start a game server with /games start"));
        }
    }
}
