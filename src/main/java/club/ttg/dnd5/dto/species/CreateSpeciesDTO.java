package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
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
public class CreateSpeciesDTO extends BaseDTO implements HasSourceDTO {
    private CreaturePropertiesDTO creatureProperties;
    private Collection<SpeciesFeatureResponse> features = new ArrayList<>();
    boolean parent;

    @Override
    public Short getPage() {
        if (this.getSource() != null) {
            return this.getSourceDTO().getPage();
        } else {
            return -1;
        }
    }

    @Override
    public void setPage(Short page) {
        this.getSourceDTO().setPage(page);
    }

    @Override
    public void setSource(String source) {
        this.getSourceDTO().setSource(source);
    }

    @Override
    public String getSource() {
        return this.getSourceDTO().getSource();
    }
}
