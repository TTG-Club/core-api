package club.ttg.dnd5.domain.beastiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BeastCategory {
    /**
     * Тип существа
     */
    @Enumerated(EnumType.STRING)
    private BeastType type;
    /**
     * Уточнения типа существа
     */
    private String text;
}