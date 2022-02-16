package dev.nincodedo.ocwgameservercommander;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class GameServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    private Long id;
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
        if (online) {
            stringBuilder.append("\nOnline");
        } else {
            stringBuilder.append("\nOffline");
        }
        return stringBuilder.toString();
    }

    public String getGameTitle() {
        return name + " - " + game;
    }
}
