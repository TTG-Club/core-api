package club.ttg.dnd5.domain.glossary.model;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Glossary extends NamedEntity {
    /**
     * Некоторые записи содержат тег в скобках после названия записи, как, например, «Атака [Действие]».
     */
    private String tags;
}
