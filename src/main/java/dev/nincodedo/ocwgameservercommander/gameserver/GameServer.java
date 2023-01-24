package dev.nincodedo.ocwgameservercommander.gameserver;

import dev.nincodedo.ocwgameservercommander.common.TimestampedEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
public class GameServer extends TimestampedEntity {

    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false)
    private String game;
    private String connectionInfo;
    private boolean online;
    private String containerId;

    public String getGameDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(description);
        if (connectionInfo != null) {
            stringBuilder.append("\nServer: ");
            stringBuilder.append(connectionInfo);
        }
        stringBuilder.append(online ? "\n🔛 Online" : "\n📴 Offline");
        return stringBuilder.toString();
    }

    public String getGameTitle() {
        return name + " - " + game;
    }
}
