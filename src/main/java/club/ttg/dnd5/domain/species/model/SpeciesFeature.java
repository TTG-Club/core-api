package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Умение вида или происхождения. Лежит в {@code species.features} (jsonb), поэтому новые
 * поля не требуют миграции.
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class SpeciesFeature {
    private String url;
    private String name;
    private String english;
    private String description;
    private String original;

    /**
     * Уровень персонажа, с которого умение действует. {@code null} — с первого.
     *
     * <p>Своим полем, а не флагом внутри механики: уровень нужен и умению без механики
     * («Большая форма» голиафа — только текст), и выгрузке компендиума, где
     * {@code Feature.level} есть у потребителя.</p>
     */
    private Integer level;

    /** Механика влияния умения на лист персонажа; {@code null} — умение только текстовое. */
    private SpeciesMechanics mechanics;

    /** Текстовое умение: без уровня и без механики. */
    public SpeciesFeature(String url, String name, String english, String description, String original) {
        this(url, name, english, description, original, null, null);
    }
}
