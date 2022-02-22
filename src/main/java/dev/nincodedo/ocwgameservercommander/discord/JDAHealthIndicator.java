package dev.nincodedo.ocwgameservercommander.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JDAHealthIndicator implements HealthIndicator {

    private final ShardManager shardManager;

    public JDAHealthIndicator(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    @Override
    public Health health() {
        var shardStatuses = shardManager.getStatuses();
        var totalShardCount = shardStatuses.values().size();
        var connectedShardsCount = getConnectedShardsCount(shardStatuses);
        if (totalShardCount == connectedShardsCount) {
            return Health.up().build();
        } else if (connectedShardsCount > 0) {
            return Health.up()
                    .status(new Status("Partial service", String.format("%s of %s shards connected", connectedShardsCount, totalShardCount)))
                    .build();
        } else {
            return Health.down().build();
        }
    }

    private int getConnectedShardsCount(Map<JDA, JDA.Status> shardStatuses) {
        int count = 0;
        for (var status : shardStatuses.values()) {
            if (status == JDA.Status.CONNECTED) {
                count++;
            }
        }
        return count;
    }
}
