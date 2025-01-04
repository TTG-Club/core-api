package club.ttg.dnd5.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class TimestampedEntity {
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private LocalDateTime updatedAt;
}
