package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@Embeddable
public class SpeciesSize {
    /** Размер */
    @Enumerated(EnumType.STRING)
    private Collection<Size> size;
    /** Размер текстом */
    private String text;
}
