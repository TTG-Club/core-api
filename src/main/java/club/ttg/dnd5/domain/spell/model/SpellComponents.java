package club.ttg.dnd5.domain.spell.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.Type;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
@Builder
public class SpellComponents {
    private Boolean v;
    private Boolean s;
    @Type(JsonType.class)
    @Column(name = "m", columnDefinition = "jsonb")
    private MaterialComponent m;
}
