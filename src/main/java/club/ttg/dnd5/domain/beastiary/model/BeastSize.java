package club.ttg.dnd5.domain.beastiary.model;

import club.ttg.dnd5.domain.common.dictionary.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeastSize {
    /**
     * Размер
     */
    @Enumerated(EnumType.STRING)
    private Size size;
    /**
     * Текстовое описание размера
     */
    private String text;

    public String getSizeString () {
        if (text == null) {
            return size.getName();
        } else {
            return String.format("%s (%s)", size.getName(), text);
        }
    }
}
