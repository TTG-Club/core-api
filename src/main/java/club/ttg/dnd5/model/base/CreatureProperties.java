package club.ttg.dnd5.model.base;

import club.ttg.dnd5.dictionary.beastiary.BeastType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class CreatureProperties extends NamedEntity {
    private String sizes;
    @Enumerated(EnumType.STRING)
    private BeastType type;
    @Column(columnDefinition = "int default 30")
    private int speed = 30;
    private int fly;
    private int climb;
    private int swim;
    private int darkVision;
}
