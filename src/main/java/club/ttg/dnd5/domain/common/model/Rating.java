package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Rating {
    @Id
    @UuidGenerator
    private UUID id;
    private String section;
    private String url;
    @CreatedBy
    private String username;
    private double value;
}
