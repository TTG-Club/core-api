package club.ttg.dnd5.domain.species.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.List;

@Getter
@Setter
@Embeddable
public class SpeciesSize {
    /** Размер */
    @Type(
            value = ListArrayType.class,
            parameters = {
                    @Parameter(
                            name = ListArrayType.SQL_ARRAY_TYPE,
                            value = "species_size"
                    )
            }
    )
    @Column(
            name = "species_size",
            columnDefinition = "species_size[]"
    )
    private List<Size> size;
    /** Размер текстом */
    private String text;
}
