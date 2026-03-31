package club.ttg.dnd5.domain.filter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Маппинг короткого SHA-256 хэша (8 символов) к оригинальному строковому значению.
 * Используется для фильтров с длинными строковыми идентификаторами (tags и т.п.).
 */
@Entity
@Table(name = "filter_hash_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterHashMapping
{
    @Id
    @Column(length = 8)
    private String hash;

    @Convert(converter = FilterHashCategoryConverter.class)
    @Column(length = 64, nullable = false)
    private FilterHashCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;
}
