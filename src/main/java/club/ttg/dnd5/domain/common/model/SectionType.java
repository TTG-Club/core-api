package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SectionType {

    BACKGROUND("backgrounds"),
    FEAT("feats"),
    SPECIES("species"),
    SPELL("spells"),
    BESTIARY("bestiary"),
    MAGIC_ITEM("magic-items"),
    ITEM("items"),
    GLOSSARY("glossary"),
    CLASS("classes"),
    TOKEN_BORDER("token-border");

    @JsonValue
    private final String value;
}
