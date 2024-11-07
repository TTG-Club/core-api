package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.model.base.HasTags;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class SpeciesFeatureResponse extends BaseDTO implements HasTags, HasSourceDTO {
    private String description;
    private Map<String, String> tags;

    @Override
    public String getSource() {
        return this.getSourceDTO().getSource();
    }

    @Override
    public Short getPage() {
        return this.getSourceDTO().getPage();
    }

    @Override
    public void setPage(Short page) {
        this.getSourceDTO().setPage(page);
    }

    @Override
    public void setSource(String source) {
        this.getSourceDTO().setSource(source);
    }
}