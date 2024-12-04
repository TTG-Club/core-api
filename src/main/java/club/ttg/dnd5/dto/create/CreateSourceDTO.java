package club.ttg.dnd5.dto.create;

import club.ttg.dnd5.dto.base.HasTagDTO;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.TranslationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSourceDTO implements HasTagDTO {
    private NameBasedDTO name;
    private String description;
    private String year;
    private String type;
    private String image;
    private Set<String> author = new HashSet<>();
    private TranslationDTO translation;
    private Set<String> tags = new HashSet<>();
}
