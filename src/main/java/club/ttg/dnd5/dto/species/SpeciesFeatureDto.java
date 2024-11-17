package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import club.ttg.dnd5.model.base.HasTags;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SpeciesFeatureDto extends BaseDTO implements HasTags, HasSourceDTO {
    private Map<String, String> tags = new HashMap<>();

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