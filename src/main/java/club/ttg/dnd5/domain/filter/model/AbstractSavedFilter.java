package club.ttg.dnd5.domain.filter.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.UUID;


@Entity
@Table(name = "saved_filter",
        indexes = {
                @Index(name = "filter_type_user_id_index", columnList = "user_id, type")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Getter
@Setter
public class AbstractSavedFilter extends Timestamped {

    @Id
    UUID id;

    UUID userId;

    Boolean defaultFilter;

    @Column(nullable = false, updatable = false, insertable = false)
    @Enumerated(EnumType.STRING)
    FilterType type;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    FilterInfo filter;

}
