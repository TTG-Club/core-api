package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.rest.dto.BaseDto;
import club.ttg.dnd5.domain.common.GroupStrategy;
import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import club.ttg.dnd5.domain.book.model.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesDetailResponse extends BaseDto implements GroupStrategy {
    @JsonProperty(value = "properties")
    private SpeciesPropertiesDto properties = new SpeciesPropertiesDto();
    private String linkImageUrl;
    // Связанные сущности
    @JsonProperty(value = "species")
    private SpeciesDetailResponse parent;
    /**
     * Происхождения.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<SpeciesDetailResponse> lineages = new LinkedHashSet<>();

    private Collection<SpeciesFeatureResponse> features;
    private NameDto group = new NameDto();

    @Override
    public void determineGroup(Source source) {
        //хотя и кажется что группа не может быть нулл, есть сценарий когда наступает хайд, и тогда группа становится нулл
        if (group != null && source.getBookInfo() != null) {
            this.group.setName("Происхождение");
            this.group.setEnglish("Basic");
        }
    }
}

