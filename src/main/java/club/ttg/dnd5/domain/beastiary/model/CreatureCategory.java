package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.CreatureType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class CreatureCategory {
    /**
     * Типы существа
     */
    @Schema(description = "Типы существа")
    @Enumerated(EnumType.STRING)
    private Collection<CreatureType> values;
    /**
     * Уточнения типа существа
     */
    @Schema(description = "Уточнение типа существа", examples = {"демон", "дьявол"})
    private String text;
}