package club.ttg.dnd5.domain.book.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.NameResponse;
import club.ttg.dnd5.dto.base.HasTagDto;
import club.ttg.dnd5.dto.base.TranslationDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailResponse implements HasTagDto {
    private String url;
    private NameResponse name;
    private String description;
    private int year;
    private String type;
    private String image;
    private Set<String> author = new HashSet<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TranslationDto translation;
    private Set<String> tags = new HashSet<>();
}
