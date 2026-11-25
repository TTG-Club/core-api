package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.model.ImageFrame;
import club.ttg.dnd5.domain.source.model.Source;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Класс или подкласс для списка: та же таблица {@code class}, но только поля краткого ответа.
 *
 * <p>Полная запись {@link CharacterClass} несёт умения, таблицу класса, механику и эффекты
 * в jsonb. Список подклассов отдаёт все 188 записей сразу, и чтение этих колонок ради
 * названия и картинки занимало около полусекунды.</p>
 *
 * <p>Только для чтения: создаёт и правит класс {@link CharacterClass}. Имена полей совпадают
 * с ним — их читает общий поиск раздела (условия и порядок заданы по тем же колонкам).</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "class")
public class CharacterClassListItem
{
    @Id
    private String url;

    private String name;

    private String english;

    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_frame", columnDefinition = "jsonb")
    private ImageFrame imageFrame;

    @Column(name = "is_hidden_entity")
    private boolean isHiddenEntity;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "parent_url")
    private String parentUrl;

    /** Родительский класс подкласса: от него берётся картинка, если своей нет. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_url", insertable = false, updatable = false)
    private CharacterClassListItem parent;

    @ManyToOne
    @JoinColumn(name = "source")
    private Source source;

    private Long sourcePage;
}
