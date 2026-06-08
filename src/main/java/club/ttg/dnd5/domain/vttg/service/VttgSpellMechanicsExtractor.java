package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellEffect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VttgSpellMechanicsExtractor {
    private static final Pattern DICE = Pattern.compile(
            "(?iu)(\\d+)\\s*[кkd]\\s*(\\d+)(?:\\s*([+-])\\s*(\\d+))?"
    );
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

    public VttgSpellMechanics extract(Spell spell, String description) {
        String text = description == null ? "" : description;
        SpellEffect effect = spell.getEffect();
        boolean structuredHealing = effect != null && hasValues(effect.getHealingTypes());
        boolean healing = structuredHealing || hasHealing(text);
        List<String> formulas = structuredDamageFormulas(effect);
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

        return new VttgSpellMechanics(formulas, healing ? true : null, extractSaveEffect(effect, text));
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
        if (!StringUtils.hasText(damageType)) {
            return formula;
        }
        return formula + "[" + damageType + "]";
    }

    private String firstFormulaOnly(String formula) {
        if (!StringUtils.hasText(formula)) {
            return formula;
        }
        int bracket = formula.indexOf('[');
        return bracket < 0 ? formula : formula.substring(0, bracket);
    }

    private String damageTypeInFormula(String formula) {
        if (!StringUtils.hasText(formula)) {
            return null;
        }
        int start = formula.indexOf('[');
        int end = formula.indexOf(']', start + 1);
        return start < 0 || end < 0 ? null : formula.substring(start + 1, end);
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
