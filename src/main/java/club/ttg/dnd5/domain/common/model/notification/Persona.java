package club.ttg.dnd5.domain.common.model.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Entity
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String image;

    @OneToMany(mappedBy = "persona", fetch = FetchType.LAZY)
    private Collection<Notification> notifications;
    private boolean disabled;

    @CreatedBy
    private String username;
    @CreatedDate
    private LocalDateTime createdAt;
}
