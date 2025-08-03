package club.ttg.dnd5.domain.full_text_search.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FullTextSearchViewType {

    BACKGROUND("background"),
    FEAT("feat"),
    SPECIES("species"),
    SPELL("spell");

    @Getter
    @JsonValue
    private final String value;
}
