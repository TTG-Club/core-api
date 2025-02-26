package club.ttg.dnd5.domain.species.rest.dto;

import club.ttg.dnd5.domain.common.dto.BaseDto;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.GroupStrategy;
import club.ttg.dnd5.domain.common.dto.NameDto;
import club.ttg.dnd5.model.book.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SpeciesDto extends BaseDto implements DetailableDTO, GroupStrategy {
    // Включаем свойства существа через DTO
    @JsonProperty(value = "properties")
    private CreaturePropertiesDto creatureProperties = new CreaturePropertiesDto();
    private String linkImageUrl;
    // Связанные сущности
    private SpeciesDto parent;
    /**
     * Происхождения.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<SpeciesDto> lineages = new LinkedHashSet<>();

    private Collection<SpeciesFeatureDto> features;
    private NameDto group = new NameDto();
    @JsonIgnore
    private boolean isDetail = false;
    private Set<String> tags = new HashSet<>();

    @Override
    public void hideDetails() {
        if (!isDetail) {
            linkImageUrl = null;
            this.creatureProperties = null;
            this.parent = null;
            this.lineages = null;
            this.features = null;
            this.group = null;
            setDescription(null);
        }
    }

    @Override
    public void determineGroup(Source source) {
        //хотя и кажется что группа не может быть нулл, есть сценарий когда наступает хайд, и тогда группа становится нулл
        if (group != null && source.getBookInfo() != null) {
            this.group.setName("Происхождение");
            this.group.setEnglish("Basic");
            this.group.setShortName("Basic");
        }
    }
}

