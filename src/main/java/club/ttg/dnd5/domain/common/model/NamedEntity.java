package club.ttg.dnd5.domain.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@MappedSuperclass
public abstract class NamedEntity extends Timestamped implements Persistable<String> {
    @Id
    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String english;
    private String alternative;
    @Column(columnDefinition = "TEXT")
    private String description;
    /**
     * Оригинальное описание на английском языке (обычный текст, без разметки).
     * Необязательное, заполняется через формы ввода на фронте.
     */
    @Column(columnDefinition = "TEXT")
    private String original;
    private String imageUrl;
    /**
     * Indicates whether this entity should be hidden from the frontend.
     * <p>
     * If {@code true}, this entity is considered outdated or irrelevant, and it will not be included
     * in responses sent to the frontend. If {@code false}, the entity will be visible to the frontend.
     * </p>
     */
    @Column(name = "is_hidden_entity")
    private boolean isHiddenEntity = false;

    /** Версия SRD, например "5.1"; {@code null} — сущность не входит в SRD. */
    @Column(name = "srd_version")
    private String srdVersion;

    /**
     * Пустая строка означает «версии нет», а не «версия пустая». Формы админки при очистке
     * поля присылают {@code ""}, а признак принадлежности к SRD везде читается как «поле не
     * null» — и в выгрузке ({@code isSRD}), и в фильтрах «только SRD»
     * ({@code where srdVersion is not null}). Без нормализации снятая в админке пометка не
     * снималась бы ни на сайте, ни в компендиуме.
     *
     * @param srdVersion версия SRD; пустая строка и пробелы трактуются как её отсутствие
     */
    public void setSrdVersion(String srdVersion) {
        String trimmed = srdVersion == null ? null : srdVersion.trim();
        this.srdVersion = trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    @Transient
    public String getId() {
        return url;
    }

    /**
     * Определяет, является ли сущность новой (ещё не сохранённой в БД).
     * Используется Spring Data JPA для выбора между persist() и merge().
     * createdAt устанавливается БД при INSERT, поэтому null означает новую сущность.
     */
    @Override
    @Transient
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
