package dev.nincodedo.ocwgameservercommander;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityUpdater {

    private final GameServerRepository gameServerRepository;
    private final ShardManager shardManager;

    public ActivityUpdater(GameServerRepository gameServerRepository, ShardManager shardManager) {
        this.gameServerRepository = gameServerRepository;
        this.shardManager = shardManager;
        updateActivity();
    }

    @Scheduled(cron = "0 */15 * * * *")
    private void updateActivity() {
        var serverCount = gameServerRepository.findAll().stream().filter(GameServer::isOnline).count();
        if (serverCount > 0) {
            String server = serverCount == 1 ? "server" : "servers";
            shardManager.setActivity(Activity.watching(String.format("%s game %s online", serverCount, server)));
        } else {
            shardManager.setActivity(null);
        }
    }
}
