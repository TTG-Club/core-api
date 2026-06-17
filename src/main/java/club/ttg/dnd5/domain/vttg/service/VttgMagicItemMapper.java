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
 * Маппер магического предмета TTG Club в формат компендиума VTTG ({@code GameItem}, type = "equipment").
 *
 * <p>Часть полей задаётся константами выгрузки (тип, метки, флаги only-read/magical), а инвентарные
 * и доспешные поля в модели источника отсутствуют и заполняются значениями по умолчанию.</p>
 *
 * <p>Сопоставление справочников выполнено под перечисления VTTG ({@code EquipmentCategory},
 * {@code ItemRarity}). Категории без точного соответствия отображаются на близкий аналог
 * (жезл/посох → wand, зелье/свиток/оружие/доспех → wondrous). Структурных данных оружия/доспеха
 * в модели нет, поэтому все предметы отдаются как {@code equipment}.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgMagicItemMapper {
    /** Метка набора данных в формате VTTG (как в примере: source = "srd"). */
    private static final String SOURCE = "srd";
    private static final String TYPE = "equipment";
    private static final String TYPE_LABEL = "Снаряжение";

    private final VttgMarkupConverter markupConverter;

    public VttgMagicItem toVttg(MagicItem item) {
        Attunement attunement = item.getAttunement();
        boolean requiresAttunement = attunement != null && attunement.isRequires();
        String sourceKey = sourceKey(item.getSource());

        return VttgMagicItem.builder()
                .id(id(item, sourceKey))
                .name(item.getName())
                .nameEn(item.getEnglish())
                // VTTG сам отрисовывает {@roll ...} в описании предмета — сохраняем эти теги.
                .description(markupConverter.toTextKeepingRolls(item.getDescription()))
                .type(TYPE)
                .typeLabel(TYPE_LABEL)
                .quantity(1)
                // В модели MagicItem нет веса — по умолчанию 0.
                .weight(0)
                // В модели MagicItem нет стоимости — по умолчанию пусто.
                .cost("")
                .rarity(rarity(item.getRarity()))
                .equipped(false)
                .equipmentCategory(equipmentCategory(item.getCategory()))
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
     * Стабильный id в формате VTTG: "{kebab-slug url}-{sourceKey}", например "wand-of-fear-dmg".
     *
     * <p>Slug — латиница/цифры/дефис (имя файла у VTTG = id). Суффикс источника обеспечивает
     * уникальность в пределах типа, если один url встречается в разных книгах.</p>
     */
    private String id(MagicItem item, String sourceKey) {
        String slug = slug(item.getUrl());
        return slug.isEmpty() ? sourceKey : slug + "-" + sourceKey;
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /** MagicItemCategory → EquipmentCategory VTTG (light|medium|heavy|shield|trinket|ring|clothing|wand|wondrous|vehicle-equipment). */
    private String equipmentCategory(MagicItemCategory category) {
        if (category == null) {
            return "wondrous";
        }
        return switch (category) {
            case WAND, ROD, STAFF -> "wand";   // нет отдельных rod/staff — implement-аналог
            case RING -> "ring";
            // нет potion/scroll/weapon/armor в EquipmentCategory — отображаем как «чудесный предмет»
            case POTION, SCROLL, WEAPON, ARMOR, SUBJECT -> "wondrous";
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
