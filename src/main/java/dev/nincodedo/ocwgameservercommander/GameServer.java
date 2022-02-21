package dev.nincodedo.ocwgameservercommander;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

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
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDateTime;
    @LastModifiedBy
    private String modifiedBy;
    @LastModifiedDate
    private LocalDateTime modifiedDateTime;

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
