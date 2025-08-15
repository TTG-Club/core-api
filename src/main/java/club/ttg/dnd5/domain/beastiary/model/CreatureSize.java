package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureSize {
    /**
     * Размер
     */
    @Enumerated(EnumType.STRING)
    private Collection<Size> values;
    /**
     * Текстовое описание размера
     */
    private String text;
}
