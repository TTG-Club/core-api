package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.GroupStrategy;
import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.model.book.Source;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedHashSet;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SpeciesDto extends BaseDTO implements DetailableDTO, GroupStrategy {
    // Включаем свойства существа через DTO
    private CreaturePropertiesDto creatureProperties = new CreaturePropertiesDto();
    // Связанные сущности
    private LinkedSpeciesDto parent = new LinkedSpeciesDto();
    private Collection<LinkedSpeciesDto> subspecies = new LinkedHashSet<>();
    private Collection<SpeciesFeatureDto> features;
    private NameBasedDTO group = new NameBasedDTO();
    @JsonIgnore
    private boolean isDetail = false;

    @Override
    public void hideDetails() {
        if (!isDetail) {
            this.creatureProperties = null;
            this.parent = null;
            this.subspecies = null;
            this.features = null;
            this.group = null;
        }
    }

    @Override
    public void determineGroup(Source source) {
        //хотя и кажется что группа не может быть нулл, есть сценарий когда наступает хайд, и тогда группа становится нулл
        if (group != null && source.getBookInfo() != null) {
            this.group.setName("Происхождение");
            this.group.setShortName("Basic");
        }
    }
}

