package club.ttg.dnd5.domain.moderation.model;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "moderation")
public class ModerationEntity extends Timestamped {

    @Id
    @Column(nullable = false, unique = true)
    private String url;

    /**
     * Раздел
     */
    @Enumerated(EnumType.STRING)
    private SectionType sectionType;

    /**
     * Раздел
     */
    @Enumerated(EnumType.STRING)
    private StatusType statusType = StatusType.DRAFT;

    /**
     * Раздел
     */
    private String comment;
}
