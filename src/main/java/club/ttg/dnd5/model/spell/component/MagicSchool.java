package club.ttg.dnd5.model.spell.component;

import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.spell.enums.MagicSchoolEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "magic_schools")
@AllArgsConstructor
public class MagicSchool {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MagicSchoolEnum id; // Используем ENUM как ID

    @Column(nullable = false)
    private String name; // Название на русском

    @Column(nullable = false)
    private String englishName; // Название на английском

    @Column(columnDefinition = "TEXT")
    private String description; // Описание школы магии

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "sourceAcronym")
    private Book book; // Источник книги
}
