package dev.nincodedo.ocwgameservercommander.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import dev.nincodedo.ocwgameservercommander.discord.CommandListener;
import dev.nincodedo.ocwgameservercommander.discord.CommandRegistration;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AppConfiguration {

    @Bean
    public ShardManager shardManager(CommandListener commandListener, CommandRegistration commandRegistration, @Value("${discordToken}") String discordToken) {
        return DefaultShardManagerBuilder.createLight(discordToken)
                .addEventListeners(commandListener, commandRegistration)
                .setShardsTotal(-1)
                .build();
    }

    @Bean
    public DockerClient dockerClient(@Value("${dockerHost}") String dockerHost) {
        var config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        var client = new ZerodepDockerHttpClient.Builder().dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        return DockerClientImpl.getInstance(config, client);
    }
}
