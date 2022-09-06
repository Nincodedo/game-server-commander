package dev.nincodedo.ocwgameservercommander.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record Constants(String gameServerAdminId, String ocwServerId) {

    public Constants(@Value("${gameServerAdminId}") String gameServerAdminId, @Value("${ocwServerId})") String ocwServerId) {
        this.gameServerAdminId = gameServerAdminId;
        this.ocwServerId = ocwServerId;
    }
}
