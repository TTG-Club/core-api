package club.ttg.dnd5.dto.character;

import club.ttg.dnd5.dto.base.BaseDTO;
import club.ttg.dnd5.dto.base.DetailableDTO;
import club.ttg.dnd5.dto.base.HasSourceDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@Builder
public class ClassDto extends BaseDTO implements DetailableDTO, HasSourceDTO  {
    @Schema(description = "снаряжение")
    private String equipment;
    @Schema(description = "владение и мастерство")
    private ClassMasteryDto mastery;

    @Schema(description = "хит дайсы")
    private String hitDice;
    // Связанные сущности

    private String parentUrl;
    private Collection<String> subSpeciesUrls;

    private Collection<ClassFeatureDto> features;

    private boolean isDetail = false;

    @Override
    public void hideDetails() {
        if (!isDetail) {
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
