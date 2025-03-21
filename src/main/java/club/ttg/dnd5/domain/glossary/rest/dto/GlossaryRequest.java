package club.ttg.dnd5.domain.glossary.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlossaryRequest {
    private String url;
    private String name;
    private String english;
    private String alternative;
    private String description;
    private String tags;
    private String imageUrl;
}
