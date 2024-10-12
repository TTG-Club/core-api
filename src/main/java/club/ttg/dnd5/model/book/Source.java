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
        indexes = {@Index(name = "idx_name", columnList = "name, english, alternative")}
)
public class Source extends TimestampedEntity {
    @Id
    private String id; // This will be used as the sourceAcronym

    @OneToOne
    @JoinColumn(name = "book_info_id", referencedColumnName = "sourceAcronym")
    private Book bookInfo;
    private short page;
}
