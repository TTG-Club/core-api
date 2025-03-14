package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class CreatureProperties extends NamedEntity {
    /** Тип существа */
    @Enumerated(EnumType.STRING)
    private BeastType type;

    /** Размеры */
    @Embedded
    private SpeciesSize size;

    /** Скорость пешком */
    @Column(columnDefinition = "int default 30")
    private int speed;
    /** Полет */
    private Integer fly;
    /** лазание */
    private Integer climb;
    /** плавание */
    private Integer swim;
    /** темное зрение */
    private Integer darkVision;
}
