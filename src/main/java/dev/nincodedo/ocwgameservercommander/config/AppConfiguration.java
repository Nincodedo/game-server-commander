package dev.nincodedo.ocwgameservercommander.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import dev.nincodedo.ocwgameservercommander.discord.CommandListener;
import dev.nincodedo.ocwgameservercommander.discord.CommandRegistration;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

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
        try {
            return DefaultShardManagerBuilder.createDefault(discordToken)
                    .addEventListeners(commandListener, commandRegistration)
                    .setShardsTotal(-1)
                    .build();
        } catch (LoginException e) {
            log.error("Failed to login", e);
        }
        return null;
    }

    @Bean
    public DockerClientConfig dockerClientConfig() {
        return DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
    }

    @Bean
    public DockerHttpClient dockerHttpClient(DockerClientConfig dockerClientConfig) {
        return new ApacheDockerHttpClient.Builder().dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
    }

    @Bean
    public DockerClient dockerClient(DockerClientConfig config, DockerHttpClient client) {
        return DockerClientImpl.getInstance(config, client);
    }
}
