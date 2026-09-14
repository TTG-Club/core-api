package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.common.dictionary.DamageType;
import club.ttg.dnd5.domain.common.model.DamagePart;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Типы урона заклинания для фильтра каталога.
 *
 * <p>Фильтр «Тип урона» смотрит только в {@code effect.damageTypes}. Типы, записанные в
 * формулах урона тегами {@code @dmg.*}, попадают туда сами при сохранении — автору остаётся
 * отметить то, чего по формулам не видно: урон на выбор, части, идущие поочерёдно. Ту же
 * выборку делают форма на сайте (подставляет типы в поле) и миграция
 * {@code 2026-09-13-01-backfill-spell-damage-types-from-formulas} для старых записей.</p>
 */
@UtilityClass
public class SpellDamageTypes {
    private static final Pattern DAMAGE_TYPE_TAG = Pattern.compile("@dmg\\.([a-z]+)", Pattern.CASE_INSENSITIVE);
    private static final Map<String, DamageType> DAMAGE_TYPES_BY_TAG = Arrays.stream(DamageType.values())
            .collect(Collectors.toMap(type -> type.name().toLowerCase(Locale.ROOT), Function.identity()));

    /**
     * Дописывает в типы урона для фильтра типы из формул: базовых частей и тиров
     * масштабирования заговора. Отмеченные вручную типы остаются первыми и в прежнем
     * порядке, повторов нет; незнакомый тег пропускается.
     */
    public void addFromFormulas(SpellEffect effect) {
        if (effect == null) {
            return;
        }

        Set<DamageType> types = new LinkedHashSet<>();
        if (effect.getDamageTypes() != null) {
            effect.getDamageTypes().stream().filter(Objects::nonNull).forEach(types::add);
        }
        formulas(effect)
                .flatMap(formula -> DAMAGE_TYPE_TAG.matcher(formula).results())
                .map(match -> DAMAGE_TYPES_BY_TAG.get(match.group(1).toLowerCase(Locale.ROOT)))
                .filter(Objects::nonNull)
                .forEach(types::add);

        if (!types.isEmpty()) {
            effect.setDamageTypes(List.copyOf(types));
        }
    }

    private Stream<String> formulas(SpellEffect effect) {
        Stream<String> baseFormulas = effect.getDamageFormulas() == null
                ? Stream.empty()
                : effect.getDamageFormulas().stream();
        Stream<String> tierFormulas = effect.getCantripScalingTiers() == null
                ? Stream.empty()
                : effect.getCantripScalingTiers().stream()
                        .filter(Objects::nonNull)
                        .map(SpellEffect.CantripScalingTier::getParts)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .filter(Objects::nonNull)
                        .map(DamagePart::getFormula);
        return Stream.concat(baseFormulas, tierFormulas).filter(Objects::nonNull);
    }
}
