package dev.nincodedo.ocwgameservercommander.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
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
    private final String discordToken;
    private final String dockerHost;

    public AppConfiguration(@Value("${discordToken}") String discordToken, @Value("${dockerHost}") String dockerHost) {
        this.discordToken = discordToken;
        this.dockerHost = dockerHost;
    }

    @Bean
    public ShardManager shardManager(CommandListener commandListener, CommandRegistration commandRegistration) {
        return DefaultShardManagerBuilder.createLight(discordToken)
                .addEventListeners(commandListener, commandRegistration)
                .setShardsTotal(-1)
                .build();
    }

    @Bean
    public DockerClient dockerClient() {
        var config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        var client = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        return DockerClientImpl.getInstance(config, client);
    }
}
