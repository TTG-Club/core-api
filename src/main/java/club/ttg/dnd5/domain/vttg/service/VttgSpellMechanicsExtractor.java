package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgDamagePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VttgSpellMechanicsExtractor {
    private static final Pattern DICE = Pattern.compile(
            "(?iu)(\\d+)\\s*[кkd]\\s*(\\d+)(?:\\s*([+-])\\s*(\\d+))?"
    );
    private static final Pattern DAMAGE_TYPE_MARKER = Pattern.compile("@dmg\\.([A-Za-z-]+)");
    private static final Pattern HEALING_MARKER = Pattern.compile("(?i)@heal(?!\\.temp)");
    private static final Pattern TEMPORARY_HEALING_MARKER = Pattern.compile("(?i)heal\\.temp");
    private static final Pattern HEALING = Pattern.compile(
            "(?iu)(?:восстанавлив\\p{L}*|восстановить|исцел\\p{L}*|лечени\\p{L}*)"
                    + ".{0,120}?хит"
    );
    private static final Pattern NEGATED_HEALING = Pattern.compile(
            "(?iu)не\\s+(?:может\\s+|могут\\s+)?(?:восстанавлив\\p{L}*|исцел\\p{L}*)"
    );
    private static final Pattern DAMAGE = Pattern.compile("(?iu)урон\\p{L}*");
    private static final Pattern SUCCESSFUL_SAVE = Pattern.compile(
            "(?iu)(?:при\\s+успех\\p{L}*|при\\s+успешн\\p{L}*\\s+спасброск\\p{L}*"
                    + "|успешн\\p{L}*\\s+спасбросок)"
    );
    private static final Pattern HALF_DAMAGE = Pattern.compile(
            "(?iu)(?:половин\\p{L}*\\s+(?:этого\\s+)?урон\\p{L}*"
                    + "|урон\\p{L}*.{0,80}?уменьш\\p{L}*\\s+вдвое)"
    );
    private static final Pattern NO_DAMAGE = Pattern.compile(
            "(?iu)(?:не\\s+получ\\p{L}*|не\\s+нанос\\p{L}*).{0,60}?урон\\p{L}*"
    );
    private static final Map<String, Pattern> TEXT_DAMAGE_TYPES = textDamageTypes();
    private static final Set<String> DAMAGE_PART_TARGETS = Set.of("selected", "self", "choose");
    private static final String DEFAULT_DAMAGE_PART_TARGET = "selected";
    /**
     * Legacy-теги цели, которые редактор писал прямо в формулу до появления
     * {@code effect.damageFormulaTargets}. В компендиум они попасть не должны:
     * VTTG понимает в формуле только {@code @target.full}/{@code @target.notFull},
     * которые под этот шаблон не подпадают.
     */
    private static final Pattern LEGACY_TARGET_MARKER = Pattern.compile("(?i)@target\\.(self|separate)\\b");

    public VttgSpellMechanics extract(Spell spell, String description) {
        String text = description == null ? "" : description;
        SpellEffect effect = spell.getEffect();
        List<String> rawFormulas = structuredDamageFormulas(effect);
        // Цели резолвятся по «сырым» формулам (в них может остаться legacy-тег),
        // а наружу уходят формулы уже без него.
        List<String> damagePartTargets = damagePartTargets(rawFormulas, effect);
        List<String> formulas = stripLegacyTargets(rawFormulas);
        boolean structuredFormulas = formulas != null;
        boolean formulaHealing = hasHealingMarker(formulas);
        boolean structuredHealing = effect != null && hasValues(effect.getHealingTypes());
        boolean healing = formulaHealing || structuredHealing || hasHealing(text);
        String formula = formulas == null
                ? extractFormula(text, healing)
                : firstFormulaOnly(formulas.getFirst());
        String damageType = structuredDamageType(formulas);

        if (damageType == null && DAMAGE.matcher(text).find()) {
            damageType = textDamageType(text, formula);
        }

        if (formulas == null && StringUtils.hasText(formula)) {
            formulas = List.of(formatDamageFormula(formula, damageType));
        }
        String primaryFormula = hasValues(formulas) ? firstFormulaOnly(formulas.getFirst()) : formula;
        String primaryDamageType = damageType == null && hasValues(formulas)
                ? damageTypeInFormula(formulas.getFirst())
                : damageType;

        return new VttgSpellMechanics(
                primaryFormula,
                primaryDamageType,
                damageParts(
                        formulas,
                        !formulaHealing && (structuredHealing || (!structuredFormulas && healing)),
                        damagePartTargets,
                        effect == null ? null : effect.getDamageFormulaRequiresDamage()),
                healing ? true : null,
                extractSaveEffect(effect, text)
        );
    }

    private String extractSaveEffect(SpellEffect effect, String text) {
        if (effect != null && effect.getSaveEffect() != null) {
            return effect.getSaveEffect().name().toLowerCase(Locale.ROOT);
        }
        if (!SUCCESSFUL_SAVE.matcher(text).find()) {
            return null;
        }
        if (HALF_DAMAGE.matcher(text).find()) {
            return "half";
        }
        if (NO_DAMAGE.matcher(text).find()) {
            return "none";
        }
        return null;
    }

    private String extractFormula(String text, boolean healing) {
        Matcher matcher = DICE.matcher(text);
        String onlyFormula = null;
        int formulaCount = 0;

        while (matcher.find()) {
            String formula = normalizeFormula(matcher);
            formulaCount++;
            onlyFormula = formula;

            String before = window(text, matcher.start() - 100, matcher.start());
            String after = window(text, matcher.end(), matcher.end() + 100);
            if (DAMAGE.matcher(after).find()
                    || (healing && HEALING.matcher(before + matcher.group() + after).find())) {
                return formula;
            }
        }

        return formulaCount == 1 && healing ? onlyFormula : null;
    }

    private boolean hasHealing(String text) {
        Matcher matcher = HEALING.matcher(text);
        while (matcher.find()) {
            String context = window(text, matcher.start() - 25, matcher.end());
            if (!NEGATED_HEALING.matcher(context).find()) {
                return true;
            }
        }
        return false;
    }

    private List<String> structuredDamageFormulas(SpellEffect effect) {
        return effect == null || !hasValues(effect.getDamageFormulas()) ? null : effect.getDamageFormulas();
    }

    private String structuredDamageType(List<String> formulas) {
        return hasValues(formulas) ? damageTypeInFormula(formulas.getFirst()) : null;
    }

    private String textDamageType(String text, String formula) {
        String searchText = text.toLowerCase(Locale.ROOT);
        if (formula != null) {
            Matcher formulaMatcher = DICE.matcher(searchText);
            if (formulaMatcher.find()) {
                searchText = window(searchText, formulaMatcher.start() - 40, formulaMatcher.end() + 140);
            }
        }

        for (Map.Entry<String, Pattern> entry : TEXT_DAMAGE_TYPES.entrySet()) {
            if (entry.getValue().matcher(searchText).find()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String normalizeFormula(Matcher matcher) {
        String formula = matcher.group(1) + "к" + matcher.group(2);
        if (StringUtils.hasText(matcher.group(3))) {
            formula += matcher.group(3) + matcher.group(4);
        }
        return formula;
    }

    private String window(String text, int start, int end) {
        return text.substring(Math.max(0, start), Math.min(text.length(), end));
    }

    private String formatDamageFormula(String formula, String damageType) {
        formula = compactFormula(formula);
        if (!StringUtils.hasText(damageType)) {
            return formula;
        }
        Matcher matcher = Pattern.compile("(?iu)^(\\d+\\s*[кkd]\\s*\\d+)(.*)$").matcher(formula);
        if (matcher.matches()) {
            return matcher.group(1) + "@dmg." + damageType + matcher.group(2);
        }
        return formula + "@dmg." + damageType;
    }

    private String firstFormulaOnly(String formula) {
        if (!StringUtils.hasText(formula)) {
            return formula;
        }
        Matcher damageMarker = DAMAGE_TYPE_MARKER.matcher(formula);
        if (damageMarker.find()) {
            return compactFormula(damageMarker.replaceFirst(""));
        }
        int bracket = formula.indexOf('[');
        int end = formula.indexOf(']', bracket + 1);
        return compactFormula(bracket < 0 || end < 0 ? formula : formula.substring(0, bracket) + formula.substring(end + 1));
    }

    private String damageTypeInFormula(String formula) {
        if (!StringUtils.hasText(formula)) {
            return null;
        }
        Matcher damageMarker = DAMAGE_TYPE_MARKER.matcher(formula);
        if (damageMarker.find()) {
            return damageMarker.group(1);
        }
        int start = formula.indexOf('[');
        int end = formula.indexOf(']', start + 1);
        return start < 0 || end < 0 ? null : formula.substring(start + 1, end);
    }

    private boolean hasHealingMarker(List<String> formulas) {
        return hasValues(formulas) && formulas.stream()
                .anyMatch(this::isHealingFormula);
    }

    private boolean isHealingFormula(String formula) {
        return StringUtils.hasText(formula)
                && (HEALING_MARKER.matcher(formula).find()
                || TEMPORARY_HEALING_MARKER.matcher(formula).find());
    }

    /**
     * Части урона для компендиума. Цель части берётся из {@code damageFormulaTargets}
     * по индексу формулы, поэтому список обходится по индексам, а пустые формулы
     * пропускаются без сдвига выравнивания.
     */
    private List<VttgDamagePart> damageParts(List<String> formulas,
                                             boolean legacyHealing,
                                             List<String> targets,
                                             List<Boolean> requiresDamage) {
        if (!hasValues(formulas)) {
            return null;
        }
        List<VttgDamagePart> parts = new ArrayList<>();
        for (int index = 0; index < formulas.size(); index++) {
            String formula = formulas.get(index);
            if (!StringUtils.hasText(formula)) {
                continue;
            }
            parts.add(VttgDamagePart.builder()
                    .formula(applyHealMarker(normalizeDamagePartFormula(formula),
                            isHealingFormula(formula), legacyHealing))
                    .target(damagePartTarget(targets, index))
                    .requiresDamage(damagePartRequiresDamage(requiresDamage, index))
                    .build());
        }
        return parts;
    }

    private String damagePartTarget(List<String> targets, int index) {
        return targets == null || index >= targets.size()
                ? DEFAULT_DAMAGE_PART_TARGET
                : targets.get(index);
    }

    /**
     * Признак «только если нанесён урон» по индексу формулы. Ложь и пропуск
     * равнозначны дефолту VTTG, поэтому наружу уходит {@code null}: поле
     * с {@code NON_NULL} не попадёт в компендиум и не раздует его.
     */
    private Boolean damagePartRequiresDamage(List<Boolean> requiresDamage, int index) {
        if (requiresDamage == null || index >= requiresDamage.size()) {
            return null;
        }
        return Boolean.TRUE.equals(requiresDamage.get(index)) ? Boolean.TRUE : null;
    }

    /**
     * Цели частей урона по индексам формул: структурное поле
     * {@code effect.damageFormulaTargets} важнее, при пустом или неизвестном
     * значении подхватывается legacy-тег из самой формулы, иначе — {@code selected}.
     */
    private List<String> damagePartTargets(List<String> rawFormulas, SpellEffect effect) {
        if (rawFormulas == null) {
            return null;
        }
        List<String> structuredTargets = effect == null ? null : effect.getDamageFormulaTargets();
        List<String> targets = new ArrayList<>();
        for (int index = 0; index < rawFormulas.size(); index++) {
            targets.add(resolveDamagePartTarget(structuredTargets, index, rawFormulas.get(index)));
        }
        return targets;
    }

    private String resolveDamagePartTarget(List<String> structuredTargets, int index, String rawFormula) {
        String structuredTarget = structuredTargets == null || index >= structuredTargets.size()
                ? null
                : structuredTargets.get(index);
        if (structuredTarget != null && DAMAGE_PART_TARGETS.contains(structuredTarget)) {
            return structuredTarget;
        }
        String legacyTarget = legacyTarget(rawFormula);
        return legacyTarget == null ? DEFAULT_DAMAGE_PART_TARGET : legacyTarget;
    }

    private String legacyTarget(String formula) {
        if (!StringUtils.hasText(formula)) {
            return null;
        }
        Matcher marker = LEGACY_TARGET_MARKER.matcher(formula);
        if (!marker.find()) {
            return null;
        }
        return "self".equalsIgnoreCase(marker.group(1)) ? "self" : "choose";
    }

    private List<String> stripLegacyTargets(List<String> formulas) {
        return formulas == null ? null : formulas.stream().map(this::stripLegacyTarget).toList();
    }

    private String stripLegacyTarget(String formula) {
        if (!StringUtils.hasText(formula)) {
            return formula;
        }
        return LEGACY_TARGET_MARKER.matcher(formula).replaceAll("").trim();
    }

    /**
     * Кодирует «лечение» прямо в формулу токеном {@code @heal} (единый стандарт COMBAT.md —
     * флаг {@code isHealing} удалён). Токен добавляется, только если часть действительно лечащая
     * и в формуле ещё нет {@code @heal}/{@code @heal.temp}; части с уроном ({@code @dmg.*}) не трогаются.
     */
    private String applyHealMarker(String formula, boolean formulaHealing, boolean legacyHealing) {
        if (!StringUtils.hasText(formula) || hasHealMarker(formula)) {
            return formula;
        }
        boolean healing = formulaHealing || (legacyHealing && !hasDamageMarker(formula));
        return healing ? formula + "@heal" : formula;
    }

    private boolean hasHealMarker(String formula) {
        return HEALING_MARKER.matcher(formula).find() || TEMPORARY_HEALING_MARKER.matcher(formula).find();
    }

    private boolean hasDamageMarker(String formula) {
        return DAMAGE_TYPE_MARKER.matcher(formula).find();
    }

    private String normalizeDamagePartFormula(String formula) {
        String damageType = damageTypeInFormula(formula);
        if (!StringUtils.hasText(damageType)) {
            return firstFormulaOnly(formula);
        }
        return formatDamageFormula(firstFormulaOnly(formula), damageType);
    }

    private String compactFormula(String formula) {
        return StringUtils.hasText(formula)
                ? formula.replaceAll("\\s*([+-])\\s*", "$1")
                : formula;
    }

    private boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private static Map<String, Pattern> textDamageTypes() {
        Map<String, Pattern> result = new LinkedHashMap<>();
        result.put("acid", Pattern.compile("(?iu)кислот"));
        result.put("bludgeoning", Pattern.compile("(?iu)дробящ"));
        result.put("cold", Pattern.compile("(?iu)холод"));
        result.put("fire", Pattern.compile("(?iu)огн|пламен"));
        result.put("force", Pattern.compile("(?iu)силов"));
        result.put("lightning", Pattern.compile("(?iu)электр|молни"));
        result.put("necrotic", Pattern.compile("(?iu)некрот"));
        result.put("piercing", Pattern.compile("(?iu)колющ"));
        result.put("poison", Pattern.compile("(?iu)яд"));
        result.put("psychic", Pattern.compile("(?iu)психич"));
        result.put("radiant", Pattern.compile("(?iu)излучен|сияющ"));
        result.put("slashing", Pattern.compile("(?iu)рубящ"));
        result.put("thunder", Pattern.compile("(?iu)звук|гром"));
        return result;
    }
}
