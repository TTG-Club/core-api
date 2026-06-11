package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellScaling;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VttgSpellScalingExtractor {
    private static final Pattern DICE = Pattern.compile(
            "(?iu)(\\d+)\\s*[кkd]\\s*(\\d+)(?:\\s*([+-])\\s*(\\d+))?"
    );
    private static final Pattern PER_SLOT = Pattern.compile(
            "(?iu)за\\s+кажд\\p{L}*\\s+(?:уровень\\s+)?(?:ячейк\\p{L}*|круг\\p{L}*)"
    );
    private static final Pattern DAMAGE_OR_HEALING_INCREASE = Pattern.compile(
            "(?iu)(?:урон\\p{L}*|лечен\\p{L}*|восстановлен\\p{L}*)"
                    + ".{0,100}?(?:увелич\\p{L}*|возраст\\p{L}*|повыш\\p{L}*)"
    );
    private static final Pattern NUMBERED_ADDITIONAL_TARGETS = Pattern.compile(
            "(?iu)(\\d+)\\s+дополнительн\\p{L}*\\s+"
                    + "(?:цел\\p{L}*|существ\\p{L}*|снаряд\\p{L}*|луч\\p{L}*|молни\\p{L}*)"
    );
    private static final Pattern SINGLE_ADDITIONAL_TARGET = Pattern.compile(
            "(?iu)(?:одн\\p{L}*|дополнительн\\p{L}+)\\s+"
                    + "(?:цел\\p{L}*|существ\\p{L}*|снаряд\\p{L}*|луч\\p{L}*|молни\\p{L}*)"
    );
    private static final Pattern TARGET_COUNT_INCREASE = Pattern.compile(
            "(?iu)количеств\\p{L}*\\s+(?:цел\\p{L}*|существ\\p{L}*|снаряд\\p{L}*)"
                    + ".{0,100}?увелич\\p{L}*\\s+на\\s+(?:1|одн\\p{L}*)"
    );

    public VttgSpellScaling extract(Boolean upcastable, String higherLevelDescription) {
        if (!Boolean.TRUE.equals(upcastable) && !StringUtils.hasText(higherLevelDescription)) {
            return null;
        }

        String description = StringUtils.hasText(higherLevelDescription)
                ? higherLevelDescription.trim()
                : null;
        String additionalDice = extractAdditionalDice(description);
        Integer additionalTargets = extractAdditionalTargets(description);

        return VttgSpellScaling.builder()
                .additionalDice(additionalDice)
                .additionalTargets(additionalTargets)
                .description(description)
                .build();
    }

    private String extractAdditionalDice(String description) {
        if (isNotPerSlot(description) || !DAMAGE_OR_HEALING_INCREASE.matcher(description).find()) {
            return null;
        }

        Matcher matcher = DICE.matcher(description);
        String formula = null;
        int count = 0;
        while (matcher.find()) {
            formula = normalizeFormula(matcher);
            count++;
        }
        return count == 1 ? formula : null;
    }

    private Integer extractAdditionalTargets(String description) {
        if (isNotPerSlot(description)) {
            return null;
        }

        Matcher numbered = NUMBERED_ADDITIONAL_TARGETS.matcher(description);
        if (numbered.find()) {
            return Integer.parseInt(numbered.group(1));
        }
        if (SINGLE_ADDITIONAL_TARGET.matcher(description).find()
                || TARGET_COUNT_INCREASE.matcher(description).find()) {
            return 1;
        }
        return null;
    }

    private boolean isNotPerSlot(String description) {
        return !StringUtils.hasText(description) || !PER_SLOT.matcher(description).find();
    }

    private String normalizeFormula(Matcher matcher) {
        String formula = matcher.group(1) + "к" + matcher.group(2);
        if (StringUtils.hasText(matcher.group(3))) {
            formula += matcher.group(3) + matcher.group(4);
        }
        return formula;
    }
}
