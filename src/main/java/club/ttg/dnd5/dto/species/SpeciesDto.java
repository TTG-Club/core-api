package club.ttg.dnd5.dto.species;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SpeciesDto extends BaseDTO implements DetailableDTO, HasSourceDTO{
    // Включаем свойства существа через DTO
    private CreaturePropertiesDto creatureProperties = new CreaturePropertiesDto();
    // Связанные сущности
    private String parentUrl;
    private Collection<String> subSpeciesUrls;
    private Collection<SpeciesFeatureDto> features;
    private boolean isDetail = false;
    @Override
    public void hideDetails() {
        if (!isDetail) {
            this.creatureProperties = null;
            this.parentUrl = null;
            this.subSpeciesUrls = null;
            this.features = null;
        }
    }

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

