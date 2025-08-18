package club.ttg.dnd5.domain.common.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SectionType {

    BACKGROUND("background"),
    FEAT("feat"),
    SPECIES("species"),
    SPELL("spell"),
    BESTIARY("bestiary"),
    MAGIC_ITEM("magic-item"),
    ITEM("item"),
    GLOSSARY("glossary");

    @Getter
    @JsonValue
    private final String value;
}
