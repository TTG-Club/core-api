package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.beastiary.model.BeastSize;
import club.ttg.dnd5.domain.beastiary.model.BeastType;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@MappedSuperclass

public abstract class CreatureProperties extends NamedEntity {
    @OneToMany
    @JoinColumn(name = "species_id")
    private Collection<SpeciesSize> sizes;
    @Enumerated(EnumType.STRING)
    private BeastType type;
    @Column(columnDefinition = "int default 30")
    private int speed = 30;
    private Integer fly;
    private Integer climb;
    private Integer swim;
    private Integer darkVision;
}
