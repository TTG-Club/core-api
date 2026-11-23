package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Alignment;
import club.ttg.dnd5.domain.source.model.Source;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;

/**
 * Существо для списка раздела: та же таблица {@code bestiary}, но только поля краткого ответа.
 *
 * <p>Полная запись {@link Creature} несёт описание и два с лишним десятка jsonb-колонок
 * (действия, особенности, логово, эффекты). Список раздела отдаёт все существа сразу, и
 * чтение с разбором этих колонок ради имени, типа и показателя опасности занимало около
 * секунды. Эта запись читает только то, что уходит в {@code CreatureShortResponse}.</p>
 *
 * <p>Только для чтения: создаёт и правит существо {@link Creature}. Имена и типы полей
 * совпадают с ним — их читает общий поиск раздела (условия и порядок заданы по тем же
 * колонкам) и маппер краткого ответа.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "bestiary")
public class CreatureListItem
{
    @Id
    private String url;

    private String name;

    private String english;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /** Типы существа. */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureCategory types;

    /** Мировоззрение. */
    @Enumerated(EnumType.STRING)
    private Alignment alignment;

    /** Опыт. */
    private Long experience;

    /** Опыт в логове. */
    private Long experienceInLair;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private CreatureSection section;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> companions;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;

    private Long sourcePage;
}
