package dev.nincodedo.ocwgameservercommander.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record Constants(String gameServerAdminId, String gameServerAdminName, String ocwServerId) {

    public Constants(@Value("${gameServerAdminId}") String gameServerAdminId, @Value("${gameServerAdminName}") String gameServerAdminName, @Value("${ocwServerId})") String ocwServerId) {
        this.gameServerAdminId = gameServerAdminId;
        this.gameServerAdminName = gameServerAdminName;
        this.ocwServerId = ocwServerId;
    }
}
