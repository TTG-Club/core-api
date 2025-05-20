package club.ttg.dnd5.domain.beastiary.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class BeastCategory {
    /**
     * Типы существа
     */
    @Schema(description = "Типы существа")
    @Enumerated(EnumType.STRING)
    private Collection<BeastType> type;
    /**
     * Уточнения типа существа
     */
    @Schema(description = "Уточнение типа существа", examples = {"демон", "дьявол"})
    private String text;
}