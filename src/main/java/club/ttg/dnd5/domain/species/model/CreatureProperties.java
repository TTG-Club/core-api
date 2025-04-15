package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.species.rest.dto.SpeciesSizeDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.List;

@Getter
@Setter
@MappedSuperclass
public abstract class CreatureProperties extends NamedEntity {
    /** Тип существа */
    @Enumerated(EnumType.STRING)
    private BeastType type;


    @Type(JsonType.class)
    @Column(columnDefinition = "JSONB")
    private List<SpeciesSizeDto> sizes;

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
