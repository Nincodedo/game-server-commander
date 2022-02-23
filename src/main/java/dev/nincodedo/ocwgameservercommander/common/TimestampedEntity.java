package dev.nincodedo.ocwgameservercommander.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public class TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    private Long id;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDateTime;
    @LastModifiedBy
    private String modifiedBy;
    @LastModifiedDate
    private LocalDateTime modifiedDateTime;

    @PostPersist
    private void postPersist() {
        createdDateTime = LocalDateTime.now();
    }

    @PostUpdate
    private void postUpdate() {
        modifiedDateTime = LocalDateTime.now();
    }
}
