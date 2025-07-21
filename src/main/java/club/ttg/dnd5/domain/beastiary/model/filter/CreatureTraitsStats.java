package club.ttg.dnd5.domain.beastiary.model.filter;

import club.ttg.dnd5.domain.beastiary.model.Creature;
import club.ttg.dnd5.domain.beastiary.model.CreatureTrait;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreatureTraitsStats {
    public List<String> getTopTraits(List<Creature> creatures, int topN) {
        Map<String, Long> counts = creatures.stream()
                .flatMap(creature -> {
                    Collection<CreatureTrait> traits = creature.getTraits();
                    if (traits == null) {
                        return Stream.empty();
                    }
                    return traits.stream();
                })
                .map(CreatureTrait::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
