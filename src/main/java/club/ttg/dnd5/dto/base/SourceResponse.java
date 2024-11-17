package club.ttg.dnd5.dto.base;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonRootName("source")
public class SourceResponse implements HasSourceDTO {
    private NameBasedDTO name = new NameBasedDTO();
    private Short page;
    @Override
    public String getSource() {
        return name.getShortName();
    }

    @Override
    public void setSource(String sourceArcronym) {
        this.name.setShortName(sourceArcronym);
    }
    private boolean homebrew = false;
    private boolean thirdParty = false;
}