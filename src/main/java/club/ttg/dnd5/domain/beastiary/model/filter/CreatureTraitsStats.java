package club.ttg.dnd5.domain.beastiary.model.filter;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;
import club.ttg.dnd5.domain.beastiary.model.enumus.CreatureTraits;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreatureTraitsStats {
    public List<CreatureTraits> getTopTraits(List<Creature> creatures, int topN) {
        Map<CreatureTraits, Long> counts = creatures.stream()
                .flatMap(creature -> {
                    Collection<CreatureTrait> traits = creature.getTraits();
                    if (traits == null) {
                        return Stream.empty();
                    }
                    return traits.stream();
                })
                .map(CreatureTrait::getName)
                .map(name -> {
                    try {
                        return CreatureTraits.parse(name);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(traitEnum -> traitEnum, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<CreatureTraits, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
