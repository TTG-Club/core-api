package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;

@Entity
@Getter
@Setter
public class Rating {
    @Id
    @UuidGenerator
    private String id;
    private String section;
    private String url;
    @CreatedBy
    private String user;
    private byte value;
}
