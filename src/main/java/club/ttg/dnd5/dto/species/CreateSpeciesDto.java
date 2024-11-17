package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateSpeciesDto extends BaseDTO implements HasSourceDTO {
    private CreaturePropertiesDto creatureProperties = new CreaturePropertiesDto();
    private Collection<SpeciesFeatureDto> features = new ArrayList<>();

    @JsonIgnore
    @Override
    public Short getPage() {
        return (this.getSource() != null) ? this.getSourceDTO().getPage() : -1;
    }

    @Override
    public void setPage(Short page) {
        this.getSourceDTO().setPage(page);
    }

    @JsonIgnore
    @Override
    public String getSource() {
        return this.getSourceDTO().getSource();
    }

    @Override
    public void setSource(String source) {
        this.getSourceDTO().setSource(source);
    }
}
