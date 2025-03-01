package club.ttg.dnd5.domain.beastiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "beast_categories")
public class BeastCategory {
    @Id
    private Long id;
    /**
     * Тип существа
     */
    @Enumerated(EnumType.STRING)
    private BeastType type;
}