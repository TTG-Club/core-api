package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Ключи черты в схеме компендиума VTTG.
 *
 * <p>Одним местом на всех, кто на черту ссылается: саму запись черты, черту происхождения
 * предыстории и выбор черты в умении класса. Идентификатор собирается из английского
 * названия ({@code "Two-Weapon Fighting" → "srd_feat_two_weapon_fighting"}), и три копии
 * этого правила разошлись бы при первой же правке — тогда предыстория и класс ссылались бы
 * на черту, которой в компендиуме под таким ключом нет.</p>
 */
public final class VttgFeatKeys {
    private static final String FEAT_ID_PREFIX = "srd_feat_";

    /** Категория черты, у которой нет своей записи в справочнике. */
    private static final String OTHER_CATEGORY_NAME = "Прочие черты";

    private VttgFeatKeys() {
    }

    /**
     * id черты в схеме эталона.
     *
     * @param feat запись черты
     * @return {@code srd_feat_<английское название>}; без английского — по url
     */
    public static String featId(Feat feat) {
        String base = StringUtils.hasText(feat.getEnglish()) ? feat.getEnglish() : feat.getUrl();
        return FEAT_ID_PREFIX + (base == null ? "" : base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", ""));
    }

    /**
     * Категория черты так, как её несёт запись компендиума ({@code VttgFeat.category}).
     *
     * <p>Тем же значением выгружаются категории выбора черты в умении класса: потребитель
     * сверяет их с полем записи, и любая другая форма — ключ enum'а, слаг — не совпала бы
     * ни с одной чертой.</p>
     *
     * @param category категория записи; {@code null} — категория не задана
     * @return подпись категории
     */
    public static String categoryName(FeatCategory category) {
        if (category == null) {
            return OTHER_CATEGORY_NAME;
        }
        return StringUtils.capitalize(category.getName());
    }
}
