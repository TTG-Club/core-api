package club.ttg.dnd5.domain.full_text_search.model;

import club.ttg.dnd5.domain.source.model.SourceType;
import club.ttg.dnd5.domain.common.model.SectionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "full_name_search_view")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FullTextSearchView {

    @Id
    private String url;
    private String name;
    private String english;
    private String alternative;
    @Enumerated(EnumType.STRING)
    private SectionType type;
    private Boolean isHiddenEntity;
    private String bookAcronym;
    private String bookName;
    private String bookEnglishName;
    @Enumerated(EnumType.STRING)
    private SourceType bookType;
    private Integer page;

}
