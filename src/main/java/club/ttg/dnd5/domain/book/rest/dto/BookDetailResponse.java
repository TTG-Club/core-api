package club.ttg.dnd5.domain.book.rest.dto;

import club.ttg.dnd5.dto.base.HasTagDTO;
import club.ttg.dnd5.domain.common.dto.NameDto;
import club.ttg.dnd5.dto.base.TranslationDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailResponse implements HasTagDTO {
    private String url;
    private NameDto name;
    private String description;
    private int year;
    private String type;
    private String image;
    private Set<String> author = new HashSet<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslationDTO translation;
    private Set<String> tags = new HashSet<>();
}
