package club.ttg.dnd5.domain.glossary.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "glossary", indexes = {
        @Index(name = "url_index", columnList = "url"),
        @Index(name = "name_index", columnList = "name, english, alternative")
})
public class Glossary extends NamedEntity {
    /**
     * Некоторые записи содержат тег в скобках после названия записи, как, например, «Атака [Действие]».
     */
    private String tags;
}
