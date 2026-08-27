package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.species.model.mechanics.SpeciesMechanics;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    /**
     * Заклинания, которые даёт это умение.
     *
     * <p>У самого умения, а не у вида целиком: «Наследие преисподней» даёт свои три
     * заклинания, и вид, у которого их дают два разных умения, иначе не отличить от
     * вида с одним. Уровень доступа берётся у умения — второе поле рядом задавало бы
     * то же самое дважды.</p>
     *
     * <p>Заклинания вида, сохранённые до появления поля, лежат в связующей таблице
     * ({@code spell_species_affiliation}) и читаются оттуда, пока запись не пересохранят.</p>
     */
    private List<GrantedSpellRef> grantedSpells;

    /**
     * Активные эффекты умения в вокабуляре VTTG — та же модель, что у черты и предмета.
     *
     * <p>Соседом механики, а не её полем: механика описывает дары, которые лист
     * проставляет сам, а эффект меняет числа готовой формулой и уезжает на виртуальный
     * стол как есть.</p>
     */
    private List<ActiveEffect> activeEffects;

    /** Текстовое умение: без уровня, без механики, без заклинаний и без эффектов. */
    public SpeciesFeature(String url, String name, String english, String description, String original) {
        this(url, name, english, description, original, null, null, null, null);
    }
}
