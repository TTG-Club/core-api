package club.ttg.dnd5.dto.base;

import lombok.Getter;
import lombok.Setter;

/*
 В @Override можно будет добавлять кастомную логику, если понадобится
 */
@Getter
@Setter
public abstract class BaseDTO implements HasSourceResponse, HasNameResponse {
    private String url;
    private NameBasedDTO nameBasedDTO;
    private SourceResponse source;
    @Override
    public String getName() {
        return nameBasedDTO.getName();
    }

    @Override
    public String getEnglish() {
        return nameBasedDTO.getEnglish();
    }

    @Override
    public String getAlternative() {
        return nameBasedDTO.getAlternative();
    }

    @Override
    public String getDescription() {
        return nameBasedDTO.getDescription();
    }

    @Override
    public Short getPage() {
        return source != null ? source.getPage() : null;
    }

    @Override
    public String getSource() {
        return source != null ? source.getSource() : null;
    }
}
