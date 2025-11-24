package club.ttg.dnd5.domain.source.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TranslationDto {
    private Set<String> author = new HashSet<>();
    private LocalDate translationDate;
}