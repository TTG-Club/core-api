package club.ttg.dnd5.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntryType {
    entries,
    inset,
    list,
    section,
    table,
    quote
}
