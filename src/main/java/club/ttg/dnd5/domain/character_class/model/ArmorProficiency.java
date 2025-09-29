package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArmorProficiency {
    private List<ArmorCategory> category;
    private String custom;

    @Override
    public String toString() {
        return Stream.of(category.stream().map(ArmorCategory::getName).collect(Collectors.joining(",")),
                custom)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));
    }
}
