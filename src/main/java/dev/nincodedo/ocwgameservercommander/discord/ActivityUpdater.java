package dev.nincodedo.ocwgameservercommander.discord;

import dev.nincodedo.ocwgameservercommander.GameServerManager;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityUpdater {

    private final GameServerManager gameServerManager;
    private final ShardManager shardManager;

    public ActivityUpdater(GameServerManager gameServerManager, ShardManager shardManager) {
        this.gameServerManager = gameServerManager;
        this.shardManager = shardManager;
        updateActivity();
    }

    @Scheduled(cron = "0 * * * * *")
    private void checkForUpdatedActivity() {
        if(gameServerManager.isRecentChangesMade()) {
            updateActivity();
            gameServerManager.setRecentChangesMade(false);
        }
    }

    @Scheduled(cron = "0 */15 * * * *")
    private void updateActivity() {
        int serverCount = gameServerManager.getOnlineGameServerCount();
        if (serverCount > 0) {
            String server = serverCount == 1 ? "server" : "servers";
            shardManager.setActivity(Activity.watching(String.format("%s game %s online", serverCount, server)));
        } else {
            shardManager.setActivity(Activity.listening("Start a game server with /games start"));
        }
    }
}
