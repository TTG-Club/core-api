package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.magic.model.Attunement;
import club.ttg.dnd5.domain.magic.model.MagicItem;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgMagicItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Маппер магического предмета TTG Club в формат компендиума VTTG ({@code GameItem}).
 *
 * <p>Тип отдаётся родной: оружие → {@code weapon}, всё остальное → {@code equipment} с флагом
 * {@code isMagical=true} (отдельного типа «magic-item» нет). {@code section} раскладывает запись
 * по листу дерева разделов (weapons/armor/rings/wands/wondrous).</p>
 *
 * <p>Сопоставление справочников выполнено под перечисления VTTG ({@code EquipmentCategory},
 * {@code ItemRarity}). Категории без точного соответствия отображаются на близкий аналог
 * (жезл/посох → wand, зелье/свиток → wondrous). Структурных данных оружия/доспеха в модели нет,
 * поэтому weapon/armor отдаются без подробных боевых/доспешных полей.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgMagicItemMapper {
    /** Запасной ключ источника, если у предмета его нет. */
    private static final String SOURCE = "srd";

    private final VttgMarkupConverter markupConverter;

    public VttgMagicItem toVttg(MagicItem item) {
        Attunement attunement = item.getAttunement();
        boolean requiresAttunement = attunement != null && attunement.isRequires();
        String sourceKey = sourceKey(item.getSource());
        MagicItemCategory category = item.getCategory();
        boolean weapon = category == MagicItemCategory.WEAPON;

        return VttgMagicItem.builder()
                .id(id(item, sourceKey))
                .name(item.getName())
                .nameEn(item.getEnglish())
                // VTTG сам отрисовывает {@roll ...} в описании предмета — сохраняем эти теги.
                .description(markupConverter.toTextKeepingRolls(item.getDescription()))
                // Оружие отдаём родным типом "weapon", всё остальное — "equipment" (п.3 контракта).
                .type(weapon ? "weapon" : "equipment")
                .typeLabel(weapon ? "Оружие" : "Снаряжение")
                .section(section(category))
                .quantity(1)
                // В модели MagicItem нет веса — по умолчанию 0.
                .weight(0)
                // В модели MagicItem нет стоимости — по умолчанию пусто.
                .cost("")
                .rarity(rarity(item.getRarity()))
                .equipped(false)
                // У оружия своя категория (weaponCategory); для type=equipment — реальная категория.
                .equipmentCategory(weapon ? null : equipmentCategory(category))
                // Доспешные поля (baseArmorAC, maxDexBonus, stealthDisadvantage,
                // strengthRequirement) актуальны только для брони; структурных данных
                // доспеха в модели источника нет, поэтому опускаем их (как в эталоне SRD-бэкапа).
                .isMagical(true)
                .magicAttunement(requiresAttunement ? "required" : "none")
                .sourceKey(sourceKey)
                .isSRD(true)
                .isReadOnly(true)
                .build();
    }

    /**
     * Стабильный id в формате VTTG: kebab-slug от {@code url} с суффиксом источника ровно один раз,
     * например "wand-of-fear-dmg". Если {@code url} уже оканчивается на ключ источника
     * (как в данных TTG: "adamantine-armor-dmg"), повторно его не добавляем — иначе VTTG создаст дубль.
     *
     * <p>Slug — латиница/цифры/дефис (имя файла у VTTG = id).</p>
     */
    private String id(MagicItem item, String sourceKey) {
        String slug = slug(item.getUrl());
        if (slug.isEmpty()) {
            return sourceKey;
        }
        return slug.equals(sourceKey) || slug.endsWith("-" + sourceKey) ? slug : slug + "-" + sourceKey;
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /** Лист дерева разделов VTTG, в который попадёт предмет (см. {@code VttgCompendiumSections}). */
    private String section(MagicItemCategory category) {
        if (category == null) {
            return "wondrous";
        }
        return switch (category) {
            case WEAPON -> "weapons";
            case ARMOR -> "armor";
            case RING -> "rings";
            case WAND, ROD, STAFF -> "wands";
            case POTION, SCROLL, SUBJECT -> "wondrous";
        };
    }

    /** MagicItemCategory → EquipmentCategory VTTG (ring|wand|wondrous|light|medium|heavy|shield|clothing...). */
    private String equipmentCategory(MagicItemCategory category) {
        if (category == null) {
            return "wondrous";
        }
        return switch (category) {
            case RING -> "ring";
            case WAND, ROD, STAFF -> "wand";   // нет отдельных rod/staff — implement-аналог
            case ARMOR -> null;                // класс брони (light|medium|heavy|shield) в модели не задан
            case POTION, SCROLL, SUBJECT -> "wondrous";
            case WEAPON -> null;               // обрабатывается как type=weapon
        };
    }

    /** Rarity → ItemRarity VTTG (none|common|uncommon|rare|very-rare|legendary|artifact). */
    private String rarity(Rarity rarity) {
        if (rarity == null) {
            return "none";
        }
        return switch (rarity) {
            case COMMON -> "common";
            case UNCOMMON -> "uncommon";
            case RARE -> "rare";
            case VERY_RARE -> "very-rare";
            case LEGENDARY -> "legendary";
            case ARTIFACT -> "artifact";
            case VARIES, UNKNOWN -> "none";   // в ItemRarity нет «varies»/«unknown»
        };
    }

    private String sourceKey(Source source) {
        if (source == null) {
            return SOURCE;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return StringUtils.hasText(source.getAcronym())
                ? source.getAcronym().toLowerCase(Locale.ROOT)
                : SOURCE;
    }
}
