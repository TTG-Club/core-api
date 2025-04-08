package club.ttg.dnd5.domain.background.model;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.domain.feat.model.Feat;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "background",
        indexes = {
                @Index(name = "url_index", columnList = "url"),
                @Index(name = "name_index", columnList = "name, english, alternative")
        }
)
public class Background extends NamedEntity {
    private String linkImageUrl; //для изоброжения бэкграунда
    /** Характеристики */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Ability> abilities;

    /** Доступные умения. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Set<Skill> skillProficiencies;

    /** Черта */
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "feat_id")
    private Feat feat;

    /** Владение инструментами */
    private String toolProficiency;
    /** Снаряжение */
    private String equipment;
    /** Предлагаемый класс */
    private String proposeClasses;

    @ManyToOne
    @JoinColumn(name = "source")
    private Book source;
    private Long sourcePage;
}
