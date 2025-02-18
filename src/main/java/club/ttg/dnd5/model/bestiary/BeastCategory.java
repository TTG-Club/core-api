package club.ttg.dnd5.model.bestiary;

import club.ttg.dnd5.dictionary.beastiary.BeastType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    /**
     * Тэги категории например (титан)
     */
    private Set<String> tags;
}
