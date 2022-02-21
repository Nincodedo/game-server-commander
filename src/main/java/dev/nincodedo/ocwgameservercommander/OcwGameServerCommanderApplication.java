package dev.nincodedo.ocwgameservercommander;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class OcwGameServerCommanderApplication {

    private final ShardManager shardManager;

    public OcwGameServerCommanderApplication(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public static void main(String[] args) {
        SpringApplication.run(OcwGameServerCommanderApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext context) {
        return args -> {
            var shards = shardManager.getShards();
            log.info("Starting with {} shards", shards.size());
        };
    }
}
