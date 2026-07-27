package club.ttg.dnd5.domain.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Замороженный slug-хендл пользователя для url его homebrew-контента ({@code u/{handle}/{stem}}).
 * <p>
 * Заводится один раз (при первом создании homebrew) из логина и больше не меняется, даже если
 * пользователь сменит логин — иначе бы «поехали» url (PK) всего его контента. Уникален
 * регистронезависимо, поэтому разные пользователи не займут один и тот же хендл.
 */
@Entity
@Table(name = "user_handle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserHandle {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String handle;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    public UserHandle(UUID userId, String handle) {
        this.userId = userId;
        this.handle = handle;
    }
}
