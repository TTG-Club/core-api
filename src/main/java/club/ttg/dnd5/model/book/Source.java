package club.ttg.dnd5.model.book;

import club.ttg.dnd5.model.base.TimestampedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sources",
        indexes = {@Index(name = "idx_name", columnList = "id")}
)
public class Source extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceAcronym;
    private short page;

    @OneToOne
    @JoinColumn(name = "book_info_id", referencedColumnName = "sourceAcronym", insertable = false, updatable = false)
    private Book bookInfo;  // Book relationship via sourceAcronym

    public Source getSource() {
        return this;
    }
}
